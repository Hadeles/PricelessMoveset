package net.fabricmc.pricelessmoveset;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class Climb {
    public static Identifier CLIMB_CHANNEL_ID = new Identifier("pricelessmoveset:climb_channel");
    public static double CLIMBING_SPEED = 0.05;

    public boolean climbing = false;
    // Are we touching a wall in the given direction?
    public boolean touching_MINX = false;
    public boolean touching_MAXX = false;
    public boolean touching_MINZ = false;
    public boolean touching_MAXZ = false;

    public StaminaModel staminaModel;
    public KeyBinding climbKeybind;

    public Climb(StaminaModel staminaModel) {
        this.staminaModel = staminaModel;
        climbKeybind = new KeyBinding(
            "key.pricelessmoveset.climb_keybind",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category." + PricelessMoveset.MODID);
        KeyBindingHelper.registerKeyBinding(climbKeybind);
    }

    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Just for the breakpoint.
        if (climbing && !canClimb()) {
            PricelessMoveset.LOGGER.info("about to stop climbing");
        }
        
        // Consume the keyboard buffer
        while (climbKeybind.wasPressed()) {
            // Toggle climbing mode
            climbing = !climbing;
        }

        if (canClimb() == false) {
            climbing = false;
        }

        // Prevent air strafing, to stop drifting away from the wall.
        if (climbing) {
            client.player.airStrafingSpeed = 0.0f;
        } else {
            client.player.airStrafingSpeed = 0.02f;
        }

        // no more "falling from a high place" when you climb down a cliff!
        if (climbing) {
            client.player.fallDistance = 0.0f;  // Does nothing? (sus)
            resetFallDistance();
        }

        // Pause stamina regen while climbing.
        if (climbing && (staminaModel.staminaPauseTicks <= 1)) { // sus
            staminaModel.staminaPauseTicks += 1;
        }

        // No sprint climbing
        if (climbing) {
            client.player.setSprinting(false);
        }

        // Fall down if out of stamina.
        if (climbing && staminaModel.stamina <= 0) climbing = false;

        if (climbing) {
            // Handle WASD keys.
            GameOptions gameOptions = client.options;
            boolean forwardKeyPressed = gameOptions.forwardKey.isPressed();
            boolean leftKeyPressed = gameOptions.leftKey.isPressed();
            boolean backKeyPressed = gameOptions.backKey.isPressed();
            boolean rightKeyPressed = gameOptions.rightKey.isPressed();

            // Climbing uses stamina and hunger.
            if (climbing && (forwardKeyPressed || leftKeyPressed || backKeyPressed || rightKeyPressed)) {
                staminaModel.stamina -=1;
                client.player.addExhaustion(0.02f);
            }
    
            double x = 0.0;
            double y = 0.0;
            double z = 0.0;
            if (forwardKeyPressed) y += CLIMBING_SPEED;
            if (backKeyPressed) y -= CLIMBING_SPEED;
            if (rightKeyPressed) {
                if (touching_MINX) z -= CLIMBING_SPEED;
                if (touching_MAXX) z += CLIMBING_SPEED;
                if (touching_MINZ) x += CLIMBING_SPEED;
                if (touching_MAXZ) x -= CLIMBING_SPEED;
            }
            if (leftKeyPressed) {
                if (touching_MINX) z += CLIMBING_SPEED;
                if (touching_MAXX) z -= CLIMBING_SPEED;
                if (touching_MINZ) x -= CLIMBING_SPEED;
                if (touching_MAXZ) x += CLIMBING_SPEED;
            }
            client.player.setVelocity(new Vec3d(x, y, z));
        }
    }

    public boolean canClimb() {
        boolean retVal = false;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        touching_MINX = false;
        touching_MAXX = false;
        touching_MINZ = false;
        touching_MAXZ = false;

        // Are we toucing a wall?
        // For the 4 cardinal directions:
        // - Expand the box slightly in that direction
        // - Verify that the box collides (with the wall)
        {  // MINX
            Vec3d pos = player.getPos();
            pos = new Vec3d(pos.x - 0.01, pos.y - 0.01, pos.z);
            Box box = player.getDimensions(player.getPose()).getBoxAt(pos);
            if (!client.world.isSpaceEmpty(box)) {
                touching_MINX = true;
                retVal = true;
            }
        }
        {  // MAXX
            Vec3d pos = player.getPos();
            pos = new Vec3d(pos.x + 0.01, pos.y - 0.01, pos.z);
            Box box = player.getDimensions(player.getPose()).getBoxAt(pos);
            if (!client.world.isSpaceEmpty(box)) {
                touching_MAXX = true;
                retVal = true;
            }
        }
        {  // MINZ
            Vec3d pos = player.getPos();
            pos = new Vec3d(pos.x, pos.y - 0.01, pos.z - 0.01);
            Box box = player.getDimensions(player.getPose()).getBoxAt(pos);
            if (!client.world.isSpaceEmpty(box)) {
                touching_MINZ = true;
                retVal = true;
            }
        }
        {  // MAXZ
            Vec3d pos = player.getPos();
            pos = new Vec3d(pos.x, pos.y - 0.01, pos.z + 0.01);
            Box box = player.getDimensions(player.getPose()).getBoxAt(pos);
            if (!client.world.isSpaceEmpty(box)) {
                touching_MAXZ = true;
                retVal = true;
            }
        }
        return retVal;
    }

    public void resetFallDistance() {
        ClientPlayNetworking.send(CLIMB_CHANNEL_ID, PacketByteBufs.empty());
    }
}
