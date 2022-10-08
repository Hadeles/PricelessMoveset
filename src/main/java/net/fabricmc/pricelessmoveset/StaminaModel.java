package net.fabricmc.pricelessmoveset;

import net.minecraft.world.GameMode;
import net.minecraft.client.MinecraftClient;

// Keep track of the player's stamina.
public class StaminaModel {
    public static int STAMINA_PER_TICK = 1;
    public static int MAX_STAMINA = 100;
    public int stamina = MAX_STAMINA;

    public StaminaModel() {}

    // Recharge some stamina
    public void tick() {
        stamina += STAMINA_PER_TICK;
        if (stamina >= MAX_STAMINA) stamina = MAX_STAMINA;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE ||
        client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) stamina = MAX_STAMINA;
    }
}
