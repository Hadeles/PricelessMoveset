package net.fabricmc.pricelessmoveset;

// Keep track of the player's stamina.
public class StaminaModel {
    public static int STAMINA_PER_TICK = 2;
    public static int MAX_STAMINA = 100;
    public int stamina = MAX_STAMINA;

    public StaminaModel() {}

    // Recharge some stamina
    public void tick() {
        stamina += STAMINA_PER_TICK;
        if (stamina >= MAX_STAMINA) stamina = MAX_STAMINA;
    }
}
