package si.bismuth.mixins;

import net.minecraft.server.command.GameModeCommand;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import si.bismuth.BismuthServer;

@Mixin(GameModeCommand.class)
public class GameModeCommandMixin {
	@Inject(method = "parseGameMode", at = @At("RETURN"), cancellable = true)
	private void onParseGameMode(CommandSource source, String s, CallbackInfoReturnable<GameMode> cir) {
		if (!BismuthServer.server.isOnlineMode()) {
			return;
		}

		if (cir.getReturnValue() == GameMode.CREATIVE) {
			cir.setReturnValue(GameMode.SPECTATOR);
		} else if (cir.getReturnValue() == GameMode.ADVENTURE) {
			cir.setReturnValue(GameMode.SURVIVAL);
		}
	}
}
