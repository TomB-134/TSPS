package si.bismuth.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.mob.hostile.boss.EnderDragonEntity;
import net.minecraft.entity.particle.ParticleType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	private LivingEntityMixin(World world) {
		super(world);
	}

	@Redirect(method = "renderBrokenItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/LivingEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
	private void fixToolBreakEffect1(LivingEntity entity, SoundEvent sound, float volume, float pitch) {
		this.world.playSound(null, this.x, this.y, this.z, sound, this.getSoundCategory(), volume, pitch);
	}

	@Redirect(method = "renderBrokenItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/entity/particle/ParticleType;DDDDDD[I)V"))
	private void fixToolBreakEffect2(World world, ParticleType type, double x, double y, double z, double velocityX, double velocityY, double velocityZ, int... parameters) {
		((ServerWorld) world).addParticle(type, x, y, z, 0, x, y + 0.05D, z, 0.0D, parameters[0], new ItemStack(Item.byId(parameters[0])).getMetadata());
	}

	@Inject(method = "pushAwayCollidingEntities", at = @At("HEAD"), cancellable = true)
	private void optimizedCollisionCancellations(CallbackInfo ci) {
		// Mixin will take care of the cast.
		// noinspection ConstantConditions
		if (!this.isPushable() && !((LivingEntity) (Object) this instanceof EnderDragonEntity)) {
			ci.cancel();
		}
	}
}
