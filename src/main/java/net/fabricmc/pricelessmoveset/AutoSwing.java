package net.fabricmc.pricelessmoveset;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class AutoSwing {
    public boolean attackKeyWasPressed = false;
    public int tickCounter = 0;

    public AutoSwing() {}

    public void tick() {
        // If left click is held down,
        // and not rising edge of left click,
        // and the attack cooldown is over,
        //  then run the attack code.
        MinecraftClient client = MinecraftClient.getInstance();
        GameOptions gameOptions = client.options;
        boolean attackKeyIsPressed = gameOptions.attackKey.isPressed();
        boolean canAttack = attackKeyIsPressed && attackKeyWasPressed;
        attackKeyWasPressed = attackKeyIsPressed;
        if (!canAttack) return;

        // The attack cooldown must be over.
        if (client.player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        /* if rising edge of ^, make a timer of 5 ticks.
         * if the timer is (rising edge of) over, do v.
         */
        ++tickCounter;
        if (tickCounter < 10) return;

        // Check that we have a target.
        if (client.crosshairTarget == null) return;
        if (!(client.crosshairTarget instanceof EntityHitResult)) return;

        // Actually attack.
        client.interactionManager.attackEntity(client.player, ((EntityHitResult)client.crosshairTarget).getEntity());
        client.player.swingHand(Hand.MAIN_HAND);
        tickCounter = 0;
    }
}
