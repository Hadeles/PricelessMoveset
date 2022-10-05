package net.fabricmc.pricelessmoveset;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class PricelessMovesetClient implements ClientModInitializer {
	public static boolean dashKeybindIsPressedPreviousTick = false;
	public static StaminaRenderer staminaRenderer = new StaminaRenderer();
	public static Dash dash = new Dash(staminaRenderer);

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		HudRenderCallback.EVENT.register(staminaRenderer);

		KeyBinding dashKeybind = new KeyBinding(
				"key.pricelessmoveset.dash_keybind",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"category." + PricelessMoveset.MODID);

		KeyBindingHelper.registerKeyBinding(dashKeybind);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientPlayerEntity entity = client.player;
			if (entity == null) return;

			dash.tick();

			if (!dashKeybindIsPressedPreviousTick && (dashKeybind.isPressed() || dashKeybind.wasPressed())) {
				client.player.sendMessage(Text.literal("Dash Keybind rising edge!"), false);

				// Detect if other WASD keys are down
				GameOptions gameOptions = client.options;
				dash.dash(
					gameOptions.forwardKey.isPressed(),
					gameOptions.leftKey.isPressed(),
					gameOptions.backKey.isPressed(),
					gameOptions.rightKey.isPressed());
			}

			// Throw away extra key presses
			while (dashKeybind.wasPressed()) {
			}

			// Remember for next tick, is the key pressed?
			dashKeybindIsPressedPreviousTick = dashKeybind.isPressed();
		});
	}
}