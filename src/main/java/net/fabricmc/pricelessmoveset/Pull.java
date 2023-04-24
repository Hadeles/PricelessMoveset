package net.fabricmc.pricelessmoveset;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class Pull {
    public static Identifier PULL_CHANNEL_ID = new Identifier("pricelessmoveset:pull_channel");

    // Allow other classes to change the cooldown. Pull.Pull_COOLDOWN_TIME
    public static long PULL_COOLDOWN_TIME = 15;
    public static double PULL_DISTANCE = 4.5;
    public static double SPEED = 0.75;
    public long lastPullUseTime = 0L;
    KeyBinding pullKeybind;
    public boolean keybindIsPressedPreviousTick = false;

    Pull() {
        pullKeybind = new KeyBinding(
                "key.pricelessmoveset.pull_keybind",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category." + PricelessMoveset.MODID);
        KeyBindingHelper.registerKeyBinding(pullKeybind);
    }

    public ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    public void tick() {
        // Must not be holding a sword.
        if (getPlayer().getMainHandStack().getItem() instanceof net.minecraft.item.SwordItem)
            return;

        // Rising edge detection
        boolean shouldPull = !keybindIsPressedPreviousTick && (pullKeybind.isPressed() || pullKeybind.wasPressed());
        ClientPlayerEntity entity = MinecraftClient.getInstance().player;
        while (pullKeybind.wasPressed())
            ; // Consume the counter
        keybindIsPressedPreviousTick = pullKeybind.isPressed();
        if (!shouldPull)
            return;

        // Check the cooldown first
        long time = entity.getEntityWorld().getTime();
        if (time <= lastPullUseTime + PULL_COOLDOWN_TIME)
            return;

        lastPullUseTime = time;

        // Actually pull
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = getPlayer();

        // Determine the target of the pull
        if (!(client.crosshairTarget instanceof EntityHitResult))
            return;
        Entity target = ((EntityHitResult) client.crosshairTarget).getEntity();
        if (target == null)
            return;

        // Send a packet to pull the target.
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(target.getId());
        ClientPlayNetworking.send(PULL_CHANNEL_ID, buf);
    }

    // Server-side.
    // Pull target towards player.
    public static void pullTowards(Entity player, Entity target) {
        if (player.noClip || !target.isPushable())
            return;

        double dx = target.getX() - player.getX();
        double dy = target.getY() - player.getY();
        double dz = target.getZ() - player.getZ();

        // Bail out if target is too far away.
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        if (distanceSquared > PULL_DISTANCE * PULL_DISTANCE)
            return;

        // Normalize the pull vector.
        double distance = Math.sqrt(distanceSquared);
        if (distance <= 0.0)
            return;
        dx /= distance;
        dy /= distance;
        dz /= distance;
        dx *= SPEED;
        dy *= SPEED;
        dz *= SPEED;

        // Pull the target.
        target.addVelocity(-dx, -dy, -dz);
    }

    public float getFill() {
        long time = getPlayer().getEntityWorld().getTime();
        float fill = 1.0f - (float) (time - lastPullUseTime) / (float) (PULL_COOLDOWN_TIME);
        if (fill < 0.0f)
            fill = 0.0f;
        if (fill > 1.0f)
            fill = 1.0f;
        return fill;
    }
}
