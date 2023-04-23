package net.fabricmc.pricelessmoveset;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.registry.Registry;

public class PricelessMovesetClient implements ClientModInitializer {

	public static Dodge dodge = new Dodge();
	public static Pull pull = new Pull();
	// public static LedgeGrab ledgeGrab = new LedgeGrab();
	public static Pogo pogo = new Pogo();
	public static AutoSwing autoSwing = new AutoSwing();
	// public static Climb climb = new Climb();
	public static SpinAttack spinAttack = new SpinAttack();
	public static CooldownView cooldownView = new CooldownView(dodge, spinAttack);
	// public static AutoSwim autoSwim = new AutoSwim();

	public static DefaultParticleType SPIN_ATTACK_PARTICLE;

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// Spin attack
		SPIN_ATTACK_PARTICLE = Registry.register(
				Registry.PARTICLE_TYPE,
				"pricelessmoveset:spin_attack",
				FabricParticleTypes.simple(true));
		ParticleFactoryRegistry.getInstance().register(SPIN_ATTACK_PARTICLE, SpinAttackParticle.Factory::new);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Don't run until there is an entity. There lies madness!
			ClientPlayerEntity entity = client.player;
			if (entity == null)
				return;

			dodge.tick();
			pull.tick();
			// ledgeGrab.tick();
			autoSwing.tick();
			// climb.tick();
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