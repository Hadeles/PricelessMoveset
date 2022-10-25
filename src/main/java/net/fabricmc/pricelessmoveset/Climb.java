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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class Climb {
    public static Identifier CLIMB_CHANNEL_ID = new Identifier("pricelessmoveset:climb_channel");
    public static enum DIRECTION {
        NONE,
        MINX,
        MAXX,
        MINZ,
        MAXZ
    };

    public boolean climbing = false;
    public DIRECTION direction = DIRECTION.NONE;
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
            client.player.fallDistance = 0.0f;  // Does nothing?
            resetFallDistance();
        }

        if (climbing) {
            // Handle WASD keys.
            GameOptions gameOptions = client.options;
            boolean forwardKeyPressed = gameOptions.forwardKey.isPressed();
            boolean leftKeyPressed = gameOptions.leftKey.isPressed();
            boolean backKeyPressed = gameOptions.backKey.isPressed();
            boolean rightKeyPressed = gameOptions.rightKey.isPressed();
    
            double x = 0.0;
            double y = 0.0;
            double z = 0.0;
            if (forwardKeyPressed) y += 0.1;
            if (backKeyPressed) y -= 0.1;
            if (rightKeyPressed) {
                switch (direction) {
                    case MINX:
                        z -= 0.1;
                    break;
                    case MAXX:
                        z += 0.1;
                    break;
                    case MINZ:
                        x += 0.1;
                    break;
                    case MAXZ:
                        x -= 0.1;
                    break;
                    default:
                }                                
            }
            if (leftKeyPressed) {
                switch (direction) {
                    case MINX:
                        z += 0.1;
                    break;
                    case MAXX:
                        z -= 0.1;
                    break;
                    case MINZ:
                        x -= 0.1;
                    break;
                    case MAXZ:
                        x += 0.1;
                    break;
                    default:
                }                                
            }
            client.player.setVelocity(new Vec3d(x, y, z));
        }
    }

    public boolean canClimb() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        // Are we toucing a wall?
        // For the 4 cardinal directions:
        // - Expand the box slightly in that direction
        // - Verify that the box collides (with the wall)
        {  // MINX
            Vec3d pos = player.getPos();
            pos = new Vec3d(pos.x - 0.0001, pos.y, pos.z);
            Box box = player.getDimensions(player.getPose()).getBoxAt(pos);
            if (!client.world.isSpaceEmpty(box)) {
                direction = DIRECTION.MINX;
                return true;
            }
        }
        {  // MAXX
            Vec3d pos = player.getPos();
            pos = new Vec3d(pos.x + 0.0001, pos.y, pos.z);
            Box box = player.getDimensions(player.getPose()).getBoxAt(pos);
            if (!client.world.isSpaceEmpty(box)) {
                direction = DIRECTION.MAXX;
                return true;
            }
        }
        {  // MINZ
            Vec3d pos = player.getPos();
            pos = new Vec3d(pos.x, pos.y, pos.z - 0.0001);
            Box box = player.getDimensions(player.getPose()).getBoxAt(pos);
            if (!client.world.isSpaceEmpty(box)) {
                direction = DIRECTION.MINZ;
                return true;
            }
        }
        {  // MAXZ
            Vec3d pos = player.getPos();
            pos = new Vec3d(pos.x, pos.y, pos.z + 0.0001);
            Box box = player.getDimensions(player.getPose()).getBoxAt(pos);
            if (!client.world.isSpaceEmpty(box)) {
                direction = DIRECTION.MAXZ;
                return true;
            }
        }
        return false;
    }

    public void resetFallDistance() {
        ClientPlayNetworking.send(CLIMB_CHANNEL_ID, PacketByteBufs.empty());
    }
}
