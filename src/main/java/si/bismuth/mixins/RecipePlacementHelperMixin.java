package si.bismuth.mixins;

import net.minecraft.crafting.recipe.Recipe;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.ResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.crafting.RecipePlacementHelper;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import si.bismuth.utils.IRecipeBookItemDuper;

@Mixin(RecipePlacementHelper.class)
public class RecipePlacementHelperMixin {
	@Shadow
	private ResultInventory resultInventory;

	@Shadow
	private CraftingInventory craftingInventory;

	@Inject(method = "clickRecipe*", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/crafting/RecipePlacementHelper;takeBackIngredients()V", shift = At.Shift.AFTER))
	private void dupingBug(ServerPlayerEntity player, Recipe recipe, boolean hasShiftDown, CallbackInfo ci) {
		this.craftingWindowDupingBugAddedBack(player);
	}

	@Unique
	private void craftingWindowDupingBugAddedBack(ServerPlayerEntity player) {
		final int slot = ((IRecipeBookItemDuper) player).bismuthServer$getDupeItem();
		if (slot == Integer.MIN_VALUE || slot == -1) {
			return;
		}

		final ItemStack dupeItem = player.inventory.getStack(slot);
		if (dupeItem.isEmpty()) {
			return;
		}

		int size = dupeItem.getSize();
		for (int j = 0; j < this.craftingInventory.getSize(); ++j) {
			final ItemStack itemstack = this.craftingInventory.getStack(j);
			if (!itemstack.isEmpty()) {
				size += itemstack.getSize();
				itemstack.setSize(0);
			}
		}

		dupeItem.setSize(size);
		this.resultInventory.clear();
		((IRecipeBookItemDuper) player).bismuthServer$clearDupeItem();
	}
}
