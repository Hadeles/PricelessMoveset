package net.fabricmc.pricelessmoveset;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayChannelHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class PricelessMovesetClient implements ClientModInitializer {
	public static boolean dodgeKeybindIsPressedPreviousTick = false;
	public static StaminaModel staminaModel = new StaminaModel();
	public static StaminaView staminaView = new StaminaView(staminaModel);
	public static Dodge dodge = new Dodge(staminaModel);

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		HudRenderCallback.EVENT.register(staminaView);

		KeyBinding dodgeKeybind = new KeyBinding(
				"key.pricelessmoveset.dodge_keybind",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"category." + PricelessMoveset.MODID);

		KeyBindingHelper.registerKeyBinding(dodgeKeybind);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientPlayerEntity entity = client.player;
			if (entity == null) return;

			GameOptions gameOptions = client.options;
			staminaModel.tick();
			dodge.tick();
			AutoSwim.tick();
			LedgeGrab.tick(gameOptions.jumpKey.isPressed());

			if (!dodgeKeybindIsPressedPreviousTick && (dodgeKeybind.isPressed() || dodgeKeybind.wasPressed())) {
				client.player.sendMessage(Text.literal("Dodge Keybind rising edge!"), false);

				// Detect if other WASD keys are down
				dodge.dodge(
					gameOptions.forwardKey.isPressed(),
					gameOptions.leftKey.isPressed(),
					gameOptions.backKey.isPressed(),
					gameOptions.rightKey.isPressed());
					
			}

			// Throw away extra key presses
			while (dodgeKeybind.wasPressed()) {
			}

			// Remember for next tick, is the key pressed?
			dodgeKeybindIsPressedPreviousTick = dodgeKeybind.isPressed();
		});

		ClientPlayNetworking.registerGlobalReceiver(
			Pogo.POGO_CHANNEL_ID,
			(client, handler, buf, responseSender) -> {
				Pogo.doPogo(client.player);
			});
	}
}