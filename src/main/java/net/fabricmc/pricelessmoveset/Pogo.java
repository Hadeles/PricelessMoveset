package net.fabricmc.pricelessmoveset;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class Pogo {
    public static float POGO_VELOCITY = 0.2f;
    public static Identifier POGO_CHANNEL_ID = new Identifier("pricelessmoveset:pogo_channel");

    public static void tick(ServerPlayerEntity player) {
        // Are we attacking something?
        LivingEntity attacking = player.getAttacking();
        if (attacking == null) return;

        // Did we *just* attack it this frame?
        if (player.getLastAttackTime() != player.age - 1) return;

        // Was this a fully charged attack?
        //// ?

        // Are we above the "attacking"?
        if (player.getPos().y > attacking.getPos().y + attacking.getHeight() - 1.0f) {
            // Tell the client to pogo.
            ServerPlayNetworking.send(player, POGO_CHANNEL_ID, PacketByteBufs.empty());
        }
    }

    public static void doPogo(ClientPlayerEntity player) {
        // Do a pogo
        player.addVelocity(0.0f, POGO_VELOCITY, 0.0f);
    }
}
