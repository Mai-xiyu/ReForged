package net.neoforged.neoforge.network;

import net.minecraft.server.level.ServerPlayer;

/**
 * Proxy: NeoForge's PacketDistributor — stub for network packet sending.
 */
public final class PacketDistributor {
    private PacketDistributor() {}

    public static void sendToPlayer(ServerPlayer player, Object payload) {
        // Stub — actual implementation would use Forge's network channel
    }

    public static void sendToServer(Object payload) {
        // Stub
    }

    public static void sendToAllPlayers(Object payload) {
        // Stub
    }
}
