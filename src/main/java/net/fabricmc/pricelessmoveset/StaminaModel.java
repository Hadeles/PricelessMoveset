package net.fabricmc.pricelessmoveset;

import net.minecraft.world.GameMode;
import net.minecraft.client.MinecraftClient;

// Keep track of the player's stamina.
// TODO: make stamina at least partially serverside
public class StaminaModel {
    public static int STAMINA_PER_TICK = 1;
    public static int MAX_STAMINA = 100;
    public int stamina = MAX_STAMINA;
    public int staminaPauseTicks = 0;

    public StaminaModel() {}

    // Recharge some stamina
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Consume stamina pause ticks, or increase stamina.
        if (staminaPauseTicks > 0) {
            staminaPauseTicks -= 1;
        } else {
            stamina += STAMINA_PER_TICK;
        }
        
        // Handle sprinting
        if (stamina == 0) {
            client.player.setSprinting(false);
        }
        if (client.player.isSprinting()) {
            stamina -= 1;
        }
        // While sprinting, pause stamina regen.
        if (client.player.isSprinting() && staminaPauseTicks <= 1) { // sus
            staminaPauseTicks += 1;
        }

        // Saturate stamina at both ends.
        if (stamina >= MAX_STAMINA) stamina = MAX_STAMINA;
        if (stamina < 0) stamina = 0;
        if (client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE ||
        client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) stamina = MAX_STAMINA;
    }
}
