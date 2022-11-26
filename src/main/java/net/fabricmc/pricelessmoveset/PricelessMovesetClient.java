package net.fabricmc.pricelessmoveset;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.network.ClientPlayerEntity;

public class PricelessMovesetClient implements ClientModInitializer {
	public static long DODGE_COOLDOWN_TIME = 50;
	public static long SPIN_ATTACK_COOLDOWN_TIME = 300;

	public static StaminaModel staminaModel = new StaminaModel();
	public static StaminaView staminaView = new StaminaView(staminaModel);
	public static CooldownModel dodgeCooldownModel = new CooldownModel(DODGE_COOLDOWN_TIME);
	public static Dodge dodge = new Dodge(staminaModel, dodgeCooldownModel);
	public static LedgeGrab ledgeGrab = new LedgeGrab(staminaModel);
	public static Pogo pogo = new Pogo(staminaModel);
	public static AutoSwing autoSwing = new AutoSwing();
	public static Climb climb = new Climb(staminaModel);
	public static CooldownModel spinAttackCooldownModel = new CooldownModel(SPIN_ATTACK_COOLDOWN_TIME);
	public static SpinAttack spinAttack = new SpinAttack(staminaModel, spinAttackCooldownModel);
	public static CooldownView cooldownView = new CooldownView(dodgeCooldownModel, spinAttackCooldownModel);
	// public static AutoSwim autoSwim = new AutoSwim();

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		HudRenderCallback.EVENT.register(staminaView);
		HudRenderCallback.EVENT.register(cooldownView);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Don't run until there is an entity. There lies madness!
			ClientPlayerEntity entity = client.player;
			if (entity == null) return;

			staminaModel.tick();
			dodge.tick();
			ledgeGrab.tick();
			dodge.tick();
			autoSwing.tick();
			climb.tick();
			spinAttack.tick();
			// autoSwim.tick();
		});

		ClientPlayNetworking.registerGlobalReceiver(
			Pogo.POGO_CHANNEL_ID,
			(client, handler, buf, responseSender) -> {
				pogo.tryPogo(client.player);
			});
	}
}