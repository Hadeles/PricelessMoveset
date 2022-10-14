package net.fabricmc.pricelessmoveset;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class AutoSwim {
    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        BlockPos blockPos = player.getBlockPos();
        blockPos.add(0, 1, 0);  // Go up 1
        boolean playerBelowWater = client.world.isWater(blockPos);
        // TODO: test playerBelowWater behaviour
        if (player.isTouchingWater() && player.shouldSwimInFluids() && playerBelowWater && client.options.forwardKey.isPressed()) {
            //player.setSprinting(true);
        }

    }
}
