package net.fabricmc.pricelessmoveset;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class AutoSwing {
    public boolean attackKeyWasPressed = false;

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

        // Actually attack.
        if (client.crosshairTarget == null) return;
        if (!(client.crosshairTarget instanceof EntityHitResult)) return;
        client.interactionManager.attackEntity(client.player, ((EntityHitResult)client.crosshairTarget).getEntity());
        client.player.swingHand(Hand.MAIN_HAND);
    }
}
