package net.fabricmc.pricelessmoveset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.option.KeyBinding;

public class PricelessMoveset implements ModInitializer {
	
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MODID = "pricelessmoveset";
	public static String VERSION = "";
	public static int[] SEMVER;
	public static final Logger LOGGER = LoggerFactory.getLogger("pricelessmoveset");
    
	public static KeyBinding dodgeKeybind;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			server.getPlayerManager().getPlayerList().forEach(player -> {
				Pogo.tick(player);
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(
			Dodge.DODGE_CHANNEL_ID,
			(server, player, handler, buf, responseSender) -> {
				boolean invulnerable = buf.readBoolean();
				player.setInvulnerable(invulnerable);
			});

		ServerPlayNetworking.registerGlobalReceiver(
			Climb.CLIMB_CHANNEL_ID,
			(server, player, handler, buf, responseSender) -> {
				player.fallDistance = 0.0f;
			});
		}
}