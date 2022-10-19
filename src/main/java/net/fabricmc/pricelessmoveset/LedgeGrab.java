package net.fabricmc.pricelessmoveset;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public class LedgeGrab {
    public boolean wasJumpKeyPressed = false;
    public int LEDGE_GRAB_STAMINA_COST = 25;
    public StaminaModel staminaModel;

    
    LedgeGrab(StaminaModel staminaModel) {
        this.staminaModel = staminaModel;
    }

    public LedgeGrab() {}

    public void tick() {        
        MinecraftClient client = MinecraftClient.getInstance();

        // Rising edge detection
        GameOptions gameOptions = client.options;
        boolean jumpKeyIsPressed = gameOptions.jumpKey.isPressed();
        boolean jumpKeyNewlyPressed = !wasJumpKeyPressed && jumpKeyIsPressed;
        wasJumpKeyPressed = jumpKeyIsPressed;

        if (!jumpKeyNewlyPressed) return;

        // Is the player on the ground.
        ClientPlayerEntity player = client.player;
        if (player.isOnGround()) return;  // TODO: this is a bug

        // Is the player next to a ledge.
        if (!isNearLedge(player.getBlockPos())) return;

        // Have we got enough stamina?
        if (staminaModel.stamina < LEDGE_GRAB_STAMINA_COST) return;

        // OK, ledge grab.
        staminaModel.stamina -= LEDGE_GRAB_STAMINA_COST;

        // Do a jump.
        player.setVelocity(player.getVelocity().x, 0.4f, player.getVelocity().z);
        player.addExhaustion(3);
    }

    public boolean isNearLedge(BlockPos blockPos) {
        return
            (
                isLedge(blockPos.add(-1, 0, 0)) ||
                isLedge(blockPos.add(+1, 0, 0)) ||
                isLedge(blockPos.add(0, 0, 0)) ||
                isLedge(blockPos.add(0, 0, -1)) ||
                isLedge(blockPos.add(0, 0, 1))
            ) && isEmpty(blockPos.add(0, -1, 0));
    }

    public boolean isLedge(BlockPos blockPos) {
        return !isEmpty(blockPos) && isEmpty(blockPos.add(0, 1, 0));
    }

    public boolean isEmpty(BlockPos blockPos) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        World world = player.getWorld();
        BlockState blockState = player.world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        VoxelShape voxelShape = block.getCollisionShape(blockState, world, blockPos, ShapeContext.of(player));
        return voxelShape.isEmpty();
    }
}
