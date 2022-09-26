package net.fabricmc.pricelessmoveset;

import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

// TODO: rename Dash to Dodge.

public class Dash {
    public static Identifier DASH_CHANNEL_ID = new Identifier("pricelessmoveset:dash_channel");

    // Allow other classes to change the cooldown. Dash.DASH_COOLDOWN_TIME
    public static long DASH_COOLDOWN_TIME = 40;
    public static long DASH_INVULNERABILITY_TIME = 10;
    public static long DASH_NO_DRAG_TIME = 1;
    public static double SPEED = 0.6;
    public long lastDashUseTime = 0L;
    public boolean hasNoDrag = false;
    public boolean hasInvulnerability = false;
    public net.minecraft.client.network.ClientPlayerEntity entity;

    Dash(net.minecraft.client.network.ClientPlayerEntity entity) {
        this.entity = entity;
    }

    public void tick() {
        noDragTick();
        invulnerabilityTick();
    }

    public void noDragTick() {
        // Bail out if there is nothing to do.
        if (!hasNoDrag) return;

        // Bail out if it's too early.
        long time = entity.getEntityWorld().getTime();
        if (time <= lastDashUseTime + DASH_NO_DRAG_TIME) return;

        // Remove noDrag state.
        entity.setNoDrag(false);
        hasNoDrag = false;
    }

    public void invulnerabilityTick() {
        // Bail out if there is nothing to do.
        if (!hasInvulnerability) return;

        // Cancel out tick cramming for this entity.
        // If only we could change isPushable! oh well.
        antiTickCramming();

        // Bail out if it's too early.
        long time = entity.getEntityWorld().getTime();
        if (time <= lastDashUseTime + DASH_INVULNERABILITY_TIME) return;

        // Remove invulerable state.
        setInvulnerableDash(false);
    }

    public void setInvulnerableDash(boolean invulnerable) {
        hasInvulnerability = invulnerable;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(invulnerable);
        ClientPlayNetworking.send(DASH_CHANNEL_ID, buf);
        entity.setInvulnerable(invulnerable);
    }

    // LivingEntity::tickCramming, but pullTowards instead of pushAway.
    public void antiTickCramming() {
        List<Entity> list = entity.world.getOtherEntities(entity, entity.getBoundingBox(), EntityPredicates.canBePushedBy(entity));
        for (int j = 0; j < list.size(); ++j) {
            Entity entity2 = list.get(j);
            pullTowards(this.entity, entity2);
        }
    }

    // Entity::pushAwayFrom, but in reverse.
    public void pullTowards(Entity entity, Entity entity2) {
        double e;
        if (entity.isConnectedThroughVehicle(entity2)) {
            return;
        }
        if (entity2.noClip || entity.noClip) {
            return;
        }
        double d = entity2.getX() - entity.getX();
        double f = MathHelper.absMax(d, e = entity2.getZ() - entity.getZ());
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
            if (!entity.hasPassengers() && entity.isPushable()) {
                entity.addVelocity(d, 0.0, e);
            }
            if (!entity2.hasPassengers() && entity2.isPushable()) {
                entity2.addVelocity(-d, 0.0, -e);
            }
        }
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
        setInvulnerableDash(true);

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
        if (this.entity.isOnGround()) {ySpeed = 0.2;} else {ySpeed = 0.0;}
        
        double groundSpeedHandicap;
        if (this.entity.isOnGround()) {groundSpeedHandicap = 1;} else {groundSpeedHandicap = 0.6;}

        double sprintSpeedHandicap;
        if (this.entity.isSprinting()) {sprintSpeedHandicap = 0.7;} else {sprintSpeedHandicap = 1;}

        double dodgeSpeedResult;
        dodgeSpeedResult = SPEED * groundSpeedHandicap * sprintSpeedHandicap;

        entity.addVelocity(
                -Math.sin(yaw) * dodgeSpeedResult, ySpeed, Math.cos(yaw) * dodgeSpeedResult);
    }
}
