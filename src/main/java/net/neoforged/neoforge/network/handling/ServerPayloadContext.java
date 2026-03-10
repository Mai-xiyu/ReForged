package net.neoforged.neoforge.network.handling;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.xiyu.reforged.shim.network.PayloadChannelRegistry;

/**
 * Server-side payload context implementation.
 */
public class ServerPayloadContext implements IPayloadContext {
    private final ServerPlayer player;

    public ServerPayloadContext(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public Connection connection() {
        return player.connection.getConnection();
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public void reply(CustomPacketPayload payload) {
        PayloadChannelRegistry.sendViaConnection(connection(), payload);
    }

    @Override
    public void disconnect(Component reason) {
        if (player != null && player.connection != null) {
            player.connection.disconnect(reason);
        }
    }

    @Override
    public CompletableFuture<Void> enqueueWork(Runnable work) {
        if (player != null && player.getServer() != null) {
            player.getServer().execute(work);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
        if (player != null && player.getServer() != null) {
            return CompletableFuture.supplyAsync(task::get, player.getServer());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public PacketFlow flow() {
        return PacketFlow.SERVERBOUND;
    }
}
