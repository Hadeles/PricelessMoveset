package net.fabricmc.pricelessmoveset;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.util.math.Box;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;

public class SpinAttack {
    public MinecraftClient client;
    public StaminaModel staminaModel;
    public KeyBinding spinAttackKeybind;
    public boolean keybindIsPressedPreviousTick = false;

    SpinAttack(StaminaModel staminaModel) {
        client = MinecraftClient.getInstance();
        this.staminaModel = staminaModel;

        spinAttackKeybind = new KeyBinding(
            "key.pricelessmoveset.spinAttack_keybind",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category." + PricelessMoveset.MODID);
        KeyBindingHelper.registerKeyBinding(spinAttackKeybind);
    }

    public void tick() {
        // Did the user press the attack button?
        if (!spinAttackKeybind.wasPressed()) return;  // Rising edge later?

        // TODO: send a packet to the server, and run the spin attack there.

        // Spin attack each of the entities nearby
        double radius = 3.0;
        Vec3d vec3d = client.player.getPos();
        double minX = vec3d.x - radius;
        double maxX = vec3d.x + radius;
        double minY = vec3d.y + (double)(client.player.getHeight() * 0.25);
        double maxY = vec3d.y + (double)(client.player.getHeight() * 0.75);
        double minZ = vec3d.z - radius;
        double maxZ = vec3d.z + radius;
        List<Entity> list = client.player.world.getOtherEntities(client.player, new Box(minX, minY, minZ, maxX, maxY, maxZ));
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
                if (client.player.isTeammate(livingEntity)) continue;
                if (livingEntity instanceof ArmorStandEntity && ((ArmorStandEntity)livingEntity).isMarker()) continue;
            }

            // Attack the entity.
            spinAttackOne(entity);
        }

        client.player.addExhaustion(0.1f);
        client.player.world.playSound(null, client.player.getX(), client.player.getY(), client.player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, client.player.getSoundCategory(), 1.0f, 1.0f);
        client.player.spawnSweepAttackParticles();
    }

    public void spinAttackOne(Entity target) {
        // Copied with modifications from PlayerEntity.attack.
        if (!target.isAttackable()) {
            return;
        }
        if (target.handleAttack(client.player)) {
            return;
        }
        float f = 2.0f * (float)client.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float g = target instanceof LivingEntity ?
            EnchantmentHelper.getAttackDamage(client.player.getMainHandStack(), ((LivingEntity)target).getGroup()) :
            EnchantmentHelper.getAttackDamage(client.player.getMainHandStack(), EntityGroup.DEFAULT);
        float h = client.player.getAttackCooldownProgress(0.5f);
        g *= h;
        client.player.resetLastAttackedTicks();
        f *= 0.2f + h * h * 0.8f;
        if (f > 0.0f || g > 0.0f) {
            ItemStack itemStack;
            boolean bl = h > 0.9f;
            boolean bl2 = false;
            int i = 0;
            i += EnchantmentHelper.getKnockback(client.player);
            if (client.player.isSprinting() && bl) {
                client.player.world.playSound(null, client.player.getX(), client.player.getY(), client.player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, client.player.getSoundCategory(), 1.0f, 1.0f);
                ++i;
                bl2 = true;
            }
            f += g;
            float j = 0.0f;
            boolean bl5 = false;
            int k = EnchantmentHelper.getFireAspect(client.player);
            if (target instanceof LivingEntity) {
                j = ((LivingEntity)target).getHealth();
                if (k > 0 && !target.isOnFire()) {
                    bl5 = true;
                    target.setOnFireFor(1);
                }
            }
            Vec3d vec3d = target.getVelocity();
            boolean bl6 = target.damage(DamageSource.player(client.player), f);
            if (bl6) {
                if (i > 0) {  // Apply knockback
                    if (target instanceof LivingEntity) {
                        ((LivingEntity)target).takeKnockback((float)i * 0.5f, MathHelper.sin(client.player.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(client.player.getYaw() * ((float)Math.PI / 180)));
                    } else {
                        target.addVelocity(-MathHelper.sin(client.player.getYaw() * ((float)Math.PI / 180)) * (float)i * 0.5f, 0.1, MathHelper.cos(client.player.getYaw() * ((float)Math.PI / 180)) * (float)i * 0.5f);
                    }
                    client.player.setVelocity(client.player.getVelocity().multiply(0.6, 1.0, 0.6));
                    client.player.setSprinting(false);
                }
                // Sweep attack
                if (target instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity)(target);
                    livingEntity.takeKnockback(
                        0.4f,
                        target.getPos().x - client.player.getPos().x,
                        target.getPos().z - client.player.getPos().z);
                    livingEntity.damage(
                        DamageSource.player(client.player),
                        1.0f + EnchantmentHelper.getSweepingMultiplier(client.player) * f);
                }
                if (target instanceof ServerPlayerEntity && target.velocityModified) {
                    ((ServerPlayerEntity)target).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));
                    target.velocityModified = false;
                    target.setVelocity(vec3d);
                }
                client.player.world.playSound(
                    null, client.player.getX(), client.player.getY(), client.player.getZ(),
                    bl ? SoundEvents.ENTITY_PLAYER_ATTACK_STRONG : SoundEvents.ENTITY_PLAYER_ATTACK_WEAK,
                    client.player.getSoundCategory(), 1.0f, 1.0f);
                if (g > 0.0f) {
                    client.player.addEnchantedHitParticles(target);
                }
                client.player.onAttacking(target);
                if (target instanceof LivingEntity) {
                    EnchantmentHelper.onUserDamaged((LivingEntity)target, client.player);
                }
                EnchantmentHelper.onTargetDamaged(client.player, target);
                ItemStack itemStack2 = client.player.getMainHandStack();
                Entity entity = target;
                if (target instanceof EnderDragonPart) {
                    entity = ((EnderDragonPart)target).owner;
                }
                if (!client.player.world.isClient && !itemStack2.isEmpty() && entity instanceof LivingEntity) {
                    itemStack2.postHit((LivingEntity)entity, client.player);
                    if (itemStack2.isEmpty()) {
                        client.player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }
                if (target instanceof LivingEntity) {
                    float m = j - ((LivingEntity)target).getHealth();
                    client.player.increaseStat(Stats.DAMAGE_DEALT, Math.round(m * 10.0f));
                    if (k > 0) {
                        target.setOnFireFor(k * 4);
                    }
                    if (client.player.world instanceof ServerWorld && m > 2.0f) {
                        int n = (int)((double)m * 0.5);
                        ((ServerWorld)client.player.world).spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.5), target.getZ(), n, 0.1, 0.0, 0.1, 0.2);
                    }
                }
            }
        }
    }
}
