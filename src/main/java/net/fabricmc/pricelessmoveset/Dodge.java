package net.fabricmc.pricelessmoveset;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class Dodge {
    public static Identifier DODGE_CHANNEL_ID = new Identifier("pricelessmoveset:dodge_channel");

    // Allow other classes to change the cooldown. Dodge.Dodge_COOLDOWN_TIME
    public static long DODGE_COOLDOWN_TIME = 50;
    public static long DODGE_INVULNERABILITY_TIME = 10;
    public static long DODGE_NO_DRAG_TIME = 1;
    public static int DODGE_STAMINA_COST = 25;
    public static double SPEED = 0.5;
    public long lastDodgeUseTime = 0L;
    public boolean hasNoDrag = false;
    public boolean hasInvulnerability = false;
    public StaminaModel staminaModel;
    KeyBinding dodgeKeybind;
    public boolean keybindIsPressedPreviousTick = false;

    Dodge(StaminaModel staminaModel) {
        this.staminaModel = staminaModel;

        dodgeKeybind = new KeyBinding(
            "key.pricelessmoveset.dodge_keybind",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category." + PricelessMoveset.MODID);
        KeyBindingHelper.registerKeyBinding(dodgeKeybind);
    }

    public ClientPlayerEntity getEntity() {
        return MinecraftClient.getInstance().player;
    }

    public void noDragTick() {
        long time = getEntity().getEntityWorld().getTime();

        // Bail out if there is nothing to do.
        if (!hasNoDrag) return;

        // Bail out if it's too early.
        if (time <= lastDodgeUseTime + DODGE_NO_DRAG_TIME) return;

        // Remove noDrag state.
        getEntity().setNoDrag(false);
        hasNoDrag = false;
    }

    public void invulnerabilityTick() {
        long time = getEntity().getEntityWorld().getTime();

        // Bail out if there is nothing to do.        
        if (!hasInvulnerability) return;

        // Cancel out tick cramming for this entity.
        // If only we could change isPushable! oh well.
        antiTickCramming();

        // Bail out if it's too early.
        if (time <= lastDodgeUseTime + DODGE_INVULNERABILITY_TIME) return;

        // Remove invulerable state.
        setInvulnerableDodge(false);
    }

    public void setInvulnerableDodge(boolean invulnerable) {
        hasInvulnerability = invulnerable;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(invulnerable);
        ClientPlayNetworking.send(DODGE_CHANNEL_ID, buf);
        getEntity().setInvulnerable(invulnerable);
    }

    // LivingEntity::tickCramming, but pullTowards instead of pushAway.
    public void antiTickCramming() {
        ClientPlayerEntity entity = getEntity();
        List<Entity> list = entity.world.getOtherEntities(entity, entity.getBoundingBox(), EntityPredicates.canBePushedBy(entity));
        for (int j = 0; j < list.size(); ++j) {
            Entity entity2 = list.get(j);
            pullTowards(entity, entity2);
        }
    }

    // Entity::pushAwayFrom, but in reverse.
    public void pullTowards(Entity entity1, Entity entity2) {
        double e;
        if (entity1.isConnectedThroughVehicle(entity2)) {
            return;
        }
        if (entity2.noClip || entity1.noClip) {
            return;
        }
        double d = entity2.getX() - entity1.getX();
        double f = MathHelper.absMax(d, e = entity2.getZ() - entity1.getZ());
        if (f >= (double)0.01f) {
            f = Math.sqrt(f);
            d /= f;
            e /= f;
            double g = 1.0 / f;
            if (g > 1.0) {
                g = 1.0;
            }
            d *= g;
            e *= g;
            d *= (double)0.05f;
            e *= (double)0.05f;
            if (!entity1.hasPassengers() && entity1.isPushable()) {
                entity1.addVelocity(d, 0.0, e);
            }
            if (!entity2.hasPassengers() && entity2.isPushable()) {
                entity2.addVelocity(-d, 0.0, -e);
            }
        }
    }

    public void tick() {
        noDragTick();
        invulnerabilityTick();

        // Rising edge detection
        boolean shouldDodge = !keybindIsPressedPreviousTick && (dodgeKeybind.isPressed() || dodgeKeybind.wasPressed());
        keybindIsPressedPreviousTick = dodgeKeybind.isPressed();
        if (!shouldDodge) return;

        // Actually dodge
        MinecraftClient client = MinecraftClient.getInstance();
        GameOptions gameOptions = client.options;
        boolean forwardKeyPressed = gameOptions.forwardKey.isPressed();
        boolean leftKeyPressed = gameOptions.leftKey.isPressed();
        boolean backKeyPressed = gameOptions.backKey.isPressed();
        boolean rightKeyPressed = gameOptions.rightKey.isPressed();

        ClientPlayerEntity entity = getEntity();

        // Check the cooldown first
        long time = entity.getEntityWorld().getTime();
        if (time <= lastDodgeUseTime + DODGE_COOLDOWN_TIME) return;

        // Have we got enough stamina?
        if (staminaModel.stamina < DODGE_STAMINA_COST) return;

        // OK, do a dodge.
        lastDodgeUseTime = time;
        staminaModel.stamina -= DODGE_STAMINA_COST;

        entity.setNoDrag(true);
        hasNoDrag = true;
        setInvulnerableDodge(true);

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
        if (entity.isOnGround()) {ySpeed = 0.2;} else {ySpeed = 0.0;}
        
        double groundSpeedHandicap;
        if (entity.isOnGround()) {groundSpeedHandicap = 1;} else {groundSpeedHandicap = 0.6;}

        double sprintSpeedHandicap;
        if (entity.isSprinting()) {sprintSpeedHandicap = 0.7;} else {sprintSpeedHandicap = 1;}

        double dodgeSpeedResult;
        dodgeSpeedResult = SPEED * groundSpeedHandicap * sprintSpeedHandicap;

        entity.addVelocity(
                -Math.sin(yaw) * dodgeSpeedResult, ySpeed, Math.cos(yaw) * dodgeSpeedResult);
                entity.addExhaustion(5);
    }
}
