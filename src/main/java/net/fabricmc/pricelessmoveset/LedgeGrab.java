package net.fabricmc.pricelessmoveset;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public class LedgeGrab {
    public static void tick(boolean jumpKey) {
        // Is the player trying to jump?
        if (!jumpKey) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        // Is the player on the ground.
        if (player.isOnGround()) return;

        // Is the player next to a ledge.
        if (!isNearLedge(player.getBlockPos())) return;

        // Do a jump.
        player.setVelocity(0.0f, 0.42f, 0.0f);
    }

    public static boolean isNearLedge(BlockPos blockPos) {
        return
            isLedge(blockPos.add(-1, 0, 0)) ||
            isLedge(blockPos.add(+1, 0, 0)) ||
            isLedge(blockPos.add(0, 0, 0)) ||
            isLedge(blockPos.add(0, 0, -1)) ||
            isLedge(blockPos.add(0, 0, 1));
    }

    public static boolean isLedge(BlockPos blockPos) {
        return !isEmpty(blockPos) && isEmpty(blockPos.add(0, 1, 0));
    }

    public static boolean isEmpty(BlockPos blockPos) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        World world = player.getWorld();
        BlockState blockState = player.world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        VoxelShape voxelShape = block.getCollisionShape(blockState, world, blockPos, ShapeContext.of(player));
        return voxelShape.isEmpty();
    }
}
