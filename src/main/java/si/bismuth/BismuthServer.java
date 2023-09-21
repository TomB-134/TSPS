package si.bismuth;

import com.google.common.collect.Lists;
import net.minecraft.crafting.CraftingManager;
import net.minecraft.crafting.recipe.Ingredient;
import net.minecraft.crafting.recipe.Recipe;
import net.minecraft.crafting.recipe.ShapelessRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.DefaultedList;
import net.minecraft.world.GameMode;
import net.ornithemc.osl.entrypoints.api.ModInitializer;
import net.ornithemc.osl.lifecycle.api.server.MinecraftServerEvents;
import net.ornithemc.osl.networking.api.server.ServerConnectionEvents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import si.bismuth.discord.DCBot;
import si.bismuth.logging.LoggerRegistry;
import si.bismuth.network.server.ServerNetworking;
import si.bismuth.utils.HUDController;

import javax.security.auth.login.LoginException;

public class BismuthServer implements ModInitializer {
	public static final String BISMUTH_SERVER_VERSION = "1.2.6";
	public static final Logger log = LogManager.getLogger("Bismuth");
	public static final ServerNetworking networking = new ServerNetworking();
	public static MinecraftServer server;
	public static DCBot bot;
	private static final Ingredient PAPER = Ingredient.of(Items.PAPER);
	private static final Ingredient SULPHUR = Ingredient.of(Items.GUNPOWDER);
	private static final Recipe duration1 = new ShapelessRecipe("rocket", makeFirework(1), DefaultedList.of(Ingredient.EMPTY, PAPER, SULPHUR));
	private static final Recipe duration2 = new ShapelessRecipe("rocket", makeFirework(2), DefaultedList.of(Ingredient.EMPTY, PAPER, SULPHUR, SULPHUR));
	private static final Recipe duration3 = new ShapelessRecipe("rocket", makeFirework(3), DefaultedList.of(Ingredient.EMPTY, PAPER, SULPHUR, SULPHUR, SULPHUR));

	static {
		CraftingManager.register("bismuth:durationone", duration1);
		CraftingManager.register("bismuth:durationtwo", duration2);
		CraftingManager.register("bismuth:durationthree", duration3);
	}

	private static ItemStack makeFirework(int duration) {
		final NbtCompound durationTag = new NbtCompound();
		final NbtCompound fireworksTag = new NbtCompound();
		durationTag.putByte("Flight", (byte) duration);
		fireworksTag.put("Fireworks", durationTag);
		final ItemStack firework = new ItemStack(Items.FIREWORKS, 3);
		firework.setNbt(fireworksTag);
		return firework;
	}

	@Override
    public void init() {
		MinecraftServerEvents.START.register(BismuthServer::init);
		MinecraftServerEvents.LOAD_WORLD.register(BismuthServer::onServerLoaded);
		MinecraftServerEvents.STOP.register(BismuthServer::stop);
		MinecraftServerEvents.TICK_START.register(BismuthServer::tick);

		ServerConnectionEvents.LOGIN.register(BismuthServer::playerConnected);
		ServerConnectionEvents.DISCONNECT.register(BismuthServer::playerDisconnected);
	}

	public static void init(MinecraftServer server) {
		BismuthServer.server = server;
	}

	public static void onServerLoaded(MinecraftServer server) {
		server.setMotd("v" + BISMUTH_SERVER_VERSION + " \u2014 " + server.getServerMotd());
		LoggerRegistry.initLoggers(server);
		if (server.isDedicated()) {
			try {
				BismuthServer.bot = new DCBot(((DedicatedServer) server).getPropertyOrDefault("botToken", ""), server.isOnlineMode());
			} catch (LoginException | InterruptedException e) {
				throw new RuntimeException("error setting up discord bot", e);
			}
		}
	}

	public static void stop(MinecraftServer server) {
		if (server.isDedicated()) {
			BismuthServer.bot.shutDownBot();
		}
		BismuthServer.server = null;
	}

	public static void tick(MinecraftServer server) {
		HUDController.update_hud(server);
	}

	public static void playerConnected(MinecraftServer server, ServerPlayerEntity player) {
		final GameMode mode = player.interactionManager.getGameMode();
		if (mode == GameMode.CREATIVE) {
			player.setGameMode(GameMode.SPECTATOR);
		} else if (mode == GameMode.ADVENTURE) {
			player.setGameMode(GameMode.SURVIVAL);
		}

		LoggerRegistry.playerConnected(player);
		unlockCustomRecipes(player);
	}

	public static void playerDisconnected(MinecraftServer server, ServerPlayerEntity player) {
		LoggerRegistry.playerDisconnected(player);
	}

	private static void unlockCustomRecipes(ServerPlayerEntity player) {
		player.unlockRecipes(Lists.newArrayList(duration1, duration2, duration3));
	}
}