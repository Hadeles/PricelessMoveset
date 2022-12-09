package net.fabricmc.pricelessmoveset;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SpinAttack {
    public static Identifier SPIN_ATTACK_CHANNEL_ID = new Identifier("pricelessmoveset:spin_attack_channel");
    public static long SPIN_ATTACK_COOLDOWN_TIME = 300;
    public MinecraftClient client;
    public KeyBinding spinAttackKeybind;
    public boolean keybindIsPressedPreviousTick = false;
    public long lastSpinAttackUseTime = 0L;

    SpinAttack() {
        client = MinecraftClient.getInstance();

        spinAttackKeybind = new KeyBinding(
            "key.pricelessmoveset.spinAttack_keybind",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category." + PricelessMoveset.MODID);
        KeyBindingHelper.registerKeyBinding(spinAttackKeybind);
    }

    public void tick() {
         // Rising edge detection
        boolean shouldSpinAttack = !keybindIsPressedPreviousTick && (spinAttackKeybind.isPressed() || spinAttackKeybind.wasPressed());
        while (spinAttackKeybind.wasPressed());  // Consume the counter
        keybindIsPressedPreviousTick = spinAttackKeybind.isPressed();
        if (!shouldSpinAttack) return;

        // Check the cooldown first
        MinecraftClient client = MinecraftClient.getInstance();
        long time = client.player.getEntityWorld().getTime();
        if (time <= lastSpinAttackUseTime + SPIN_ATTACK_COOLDOWN_TIME) return;

        // Actually spin attack
        lastSpinAttackUseTime = time;
        ClientPlayNetworking.send(SPIN_ATTACK_CHANNEL_ID, PacketByteBufs.create());
    }

    // Server side code
    public static void spinAttack(ServerPlayerEntity player) {
        // Spin attack each of the entities nearby
        double radius = 3.0;
        Vec3d vec3d = player.getPos();
        double minX = vec3d.x - radius;
        double maxX = vec3d.x + radius;
        double minY = vec3d.y + (double)(player.getHeight() * 0.25);
        double maxY = vec3d.y + (double)(player.getHeight() * 0.75);
        double minZ = vec3d.z - radius;
        double maxZ = vec3d.z + radius;
        List<Entity> list = player.world.getOtherEntities(player, new Box(minX, minY, minZ, maxX, maxY, maxZ));
        double squaredRadius = radius * radius;
        for (Entity entity : list) {
            // Skip entities out of radius.
            if (entity.squaredDistanceTo(vec3d) > squaredRadius) continue; 

            // Skip invincible players
            if (entity instanceof PlayerEntity) {
                PlayerEntity playerEntity = (PlayerEntity)(entity);
                if (playerEntity.isSpectator()) continue;
                if (playerEntity.isCreative()) continue;
            }

            // Copy the conditions for a sweep attack.
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)(entity);
                if (player.isTeammate(livingEntity)) continue;
                if (livingEntity instanceof ArmorStandEntity && ((ArmorStandEntity)livingEntity).isMarker()) continue;
            }

            // Attack the entity.
            spinAttackOne(player, entity);
        }

        player.addExhaustion(0.1f);
        player.world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0f, 1.0f);
        spawnSpinAttackParticles(player);
    }

    public static void spawnSpinAttackParticles(ServerPlayerEntity player) {
        double d = -MathHelper.sin(player.getYaw() * ((float)Math.PI / 180));
        double e = MathHelper.cos(player.getYaw() * ((float)Math.PI / 180));
        if (player.world instanceof ServerWorld) {
            ((ServerWorld)player.world).spawnParticles(
                PricelessMovesetClient.SPIN_ATTACK_PARTICLE,
                player.getX() + d,
                player.getBodyY(0.5),
                player.getZ() + e,
                0, d, 0.0, e, 0.0);
        }
    }


    public static void spinAttackOne(ServerPlayerEntity player, Entity target) {
        // Copied with modifications from PlayerEntity.attack.
        if (!target.isAttackable()) {
            return;
        }
        if (target.handleAttack(player)) {
            return;
        }
        float f = 2.0f * (float)player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float g = target instanceof LivingEntity ?
            EnchantmentHelper.getAttackDamage(player.getMainHandStack(), ((LivingEntity)target).getGroup()) :
            EnchantmentHelper.getAttackDamage(player.getMainHandStack(), EntityGroup.DEFAULT);
        float h = player.getAttackCooldownProgress(0.5f);
        g *= h;
        player.resetLastAttackedTicks();
        f *= 0.2f + h * h * 0.8f;
        if (f > 0.0f || g > 0.0f) {
            ItemStack itemStack;
            boolean bl = h > 0.9f;
            boolean bl2 = false;
            int i = 0;
            i += EnchantmentHelper.getKnockback(player);
            if (player.isSprinting() && bl) {
                player.world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0f, 1.0f);
                ++i;
                bl2 = true;
            }
            f += g;
            float j = 0.0f;
            boolean bl5 = false;
            int k = EnchantmentHelper.getFireAspect(player);
            if (target instanceof LivingEntity) {
                j = ((LivingEntity)target).getHealth();
                if (k > 0 && !target.isOnFire()) {
                    bl5 = true;
                    target.setOnFireFor(1);
                }
            }
            Vec3d vec3d = target.getVelocity();
            boolean bl6 = target.damage(DamageSource.player(player), f);
            if (bl6) {
                if (i > 0) {  // Apply knockback
                    if (target instanceof LivingEntity) {
                        ((LivingEntity)target).takeKnockback((float)i * 0.5f, MathHelper.sin(player.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(player.getYaw() * ((float)Math.PI / 180)));
                    } else {
                        target.addVelocity(-MathHelper.sin(player.getYaw() * ((float)Math.PI / 180)) * (float)i * 0.5f, 0.1, MathHelper.cos(player.getYaw() * ((float)Math.PI / 180)) * (float)i * 0.5f);
                    }
                    player.setVelocity(player.getVelocity().multiply(0.6, 1.0, 0.6));
                    player.setSprinting(false);
                }
                // Sweep attack
                if (target instanceof LivingEntity)  {
                    LivingEntity livingEntity = (LivingEntity)(target);
                    livingEntity.takeKnockback(
                        0.2f,
                        player.getPos().x - target.getPos().x,
                        player.getPos().z - target.getPos().z);
                    livingEntity.damage(
                        DamageSource.player(player),
                        1.0f + EnchantmentHelper.getSweepingMultiplier(player) * f);
                }
                if (target instanceof ServerPlayerEntity && target.velocityModified) {
                    ((ServerPlayerEntity)target).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));
                    target.velocityModified = false;
                    target.setVelocity(vec3d);
                }
                player.world.playSound(
                    null, player.getX(), player.getY(), player.getZ(),
                    bl ? SoundEvents.ENTITY_PLAYER_ATTACK_STRONG : SoundEvents.ENTITY_PLAYER_ATTACK_WEAK,
                    player.getSoundCategory(), 1.0f, 1.0f);
                if (g > 0.0f) {
                    player.addEnchantedHitParticles(target);
                }
                player.onAttacking(target);
                if (target instanceof LivingEntity) {
                    EnchantmentHelper.onUserDamaged((LivingEntity)target, player);
                }
                EnchantmentHelper.onTargetDamaged(player, target);
                ItemStack itemStack2 = player.getMainHandStack();
                Entity entity = target;
                if (target instanceof EnderDragonPart) {
                    entity = ((EnderDragonPart)target).owner;
                }
                if (!player.world.isClient && !itemStack2.isEmpty() && entity instanceof LivingEntity) {
                    itemStack2.postHit((LivingEntity)entity, player);
                    if (itemStack2.isEmpty()) {
                        player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }
                if (target instanceof LivingEntity) {
                    float m = j - ((LivingEntity)target).getHealth();
                    player.increaseStat(Stats.DAMAGE_DEALT, Math.round(m * 10.0f));
                    if (k > 0) {
                        target.setOnFireFor(k * 4);
                    }
                    if (player.world instanceof ServerWorld && m > 2.0f) {
                        int n = (int)((double)m * 0.5);
                        ((ServerWorld)player.world).spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.5), target.getZ(), n, 0.1, 0.0, 0.1, 0.2);
                    }
                }
            }
        }
    }

    public float getFill() {
        long time = MinecraftClient.getInstance().player.getEntityWorld().getTime();
        float fill = 1.0f - (float)(time - lastSpinAttackUseTime) / (float)(SPIN_ATTACK_COOLDOWN_TIME);
        if (fill < 0.0f) fill = 0.0f;
        if (fill > 1.0f) fill = 1.0f;
        return fill;
    }
}
