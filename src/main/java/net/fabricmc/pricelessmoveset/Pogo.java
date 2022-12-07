package net.fabricmc.pricelessmoveset;

import java.util.HashMap;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class Pogo {
    public static float POGO_VELOCITY = 0.5f;
    public static Identifier POGO_CHANNEL_ID = new Identifier("pricelessmoveset:pogo_channel");
    public static HashMap<Integer, Boolean> playerToChargedAttack = new HashMap<Integer, Boolean>();

    public static void setChargedAttack(ServerPlayerEntity player, boolean chargedAttack) {
        playerToChargedAttack.put((Integer)(player.getId()), (Boolean)(chargedAttack));
    }

    public static boolean getChargedAttack(ServerPlayerEntity player) {
        Boolean result = playerToChargedAttack.get((Integer)(player.getId()));
        return result == null ? false : result.booleanValue();
    }

    // Server side only
    public static void tick(ServerPlayerEntity player) {
        // Was this a fully charged attack?
        boolean chargedAttack = getChargedAttack(player);
        setChargedAttack(player, player.getAttackCooldownProgress(0.5f) >= 1.0f);
        if (!chargedAttack) return;

        // Are we attacking something?
        LivingEntity attacking = player.getAttacking();
        if (attacking == null) return;

        // Did we *just* attack it this frame?
        if (player.getLastAttackTime() != player.age - 1) return;

        // Are we above the "attacking"?
        if (player.getPos().y > attacking.getPos().y + attacking.getHeight() - 0.5f) {
            // Tell the client it can try to pogo.
            ServerPlayNetworking.send(player, POGO_CHANNEL_ID, PacketByteBufs.empty());
        }
    }

    // Client side only
    public void tryPogo(ClientPlayerEntity player) {

        // Do a pogo
        player.addVelocity(0.0f, POGO_VELOCITY, 0.0f);
        player.addExhaustion(0.5f);
    }
}
