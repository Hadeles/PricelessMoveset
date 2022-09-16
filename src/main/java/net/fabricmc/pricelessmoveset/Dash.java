package net.fabricmc.pricelessmoveset;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.mixin.client.keybinding.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;

// TODO: rename Dash to Dodge.

public class Dash {
    // Allow other classes to change the cooldown. Dash.DASH_COOLDOWN_TIME
    public static long DASH_COOLDOWN_TIME = 40;
    public static long DASH_NO_DRAG_TIME = 1;
    public static double SPEED = 0.6;
    public long lastDashUseTime = 0L;
    public boolean hasNoDrag = false;
    public net.minecraft.entity.LivingEntity entity;

    Dash(net.minecraft.entity.LivingEntity entity) {
        this.entity = entity;
    }

    public void tick() {
        // Bail out if there is nothing to do.
        if (!hasNoDrag) return;

        // Bail out if it's too early.
        long time = entity.getEntityWorld().getTime();
        if (time <= lastDashUseTime + DASH_NO_DRAG_TIME) return;

        // Remove noDrag state.
        entity.setNoDrag(false);
        hasNoDrag = false;
    }

    public void dash(
        boolean forwardKeyPressed,
        boolean leftKeyPressed,
        boolean backKeyPressed,
        boolean rightKeyPressed) {
        // Check the cooldown first
        long time = entity.getEntityWorld().getTime();
        if (time <= lastDashUseTime + DASH_COOLDOWN_TIME) return;
        lastDashUseTime = time;

        entity.setNoDrag(true);
        hasNoDrag = true;

        // Dodge according to keys pressed
        double yaw = entity.getYaw();  // Head yaw
        if (forwardKeyPressed && leftKeyPressed) {
            yaw += 315.0;
        } else if (backKeyPressed && leftKeyPressed) {
            yaw += 225.0;
        } else if (backKeyPressed && rightKeyPressed) {
            yaw += 135.0;
        } else if (forwardKeyPressed && rightKeyPressed) {
            yaw += 45.0;
        } else if (forwardKeyPressed) {
            yaw += 0.0;
        } else if (leftKeyPressed) {
            yaw += 270.0;
        } else if (backKeyPressed) {
            yaw += 180.0;
        } else if (rightKeyPressed) {
            yaw += 90.0;
        }
        // Convert yaw from degrees (above) to radians (below)
        yaw = yaw / 180.0 * Math.PI;
        double ySpeed;
        if (this.entity.isOnGround()) {ySpeed = 0.25;} else {ySpeed = 0.0;}
        
        double groundSpeedHandicap;
        if (this.entity.isOnGround()) {groundSpeedHandicap = 1;} else {groundSpeedHandicap = 0.5;}

        double sprintSpeedHandicap;
        if (this.entity.isSprinting()) {sprintSpeedHandicap = 0.7;} else {sprintSpeedHandicap = 1;}

        double dodgeSpeedResult;
        dodgeSpeedResult = SPEED * groundSpeedHandicap * sprintSpeedHandicap;

        entity.addVelocity(
                -Math.sin(yaw) * dodgeSpeedResult, ySpeed, Math.cos(yaw) * dodgeSpeedResult);
    }
}
