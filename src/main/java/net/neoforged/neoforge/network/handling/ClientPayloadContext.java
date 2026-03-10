package net.neoforged.neoforge.network.handling;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.xiyu.reforged.shim.network.PayloadChannelRegistry;

/**
 * Client-side payload context implementation.
 */
public class ClientPayloadContext implements IPayloadContext {
    @Override
    public Connection connection() {
        return Minecraft.getInstance().getConnection().getConnection();
    }

    @Override
    public Player player() {
        return Minecraft.getInstance().player;
    }

    @Override
    public void reply(CustomPacketPayload payload) {
        PayloadChannelRegistry.sendViaConnection(connection(), payload);
    }

    @Override
    public void disconnect(Component reason) {
    }

    @Override
    public CompletableFuture<Void> enqueueWork(Runnable task) {
        Minecraft.getInstance().execute(task);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task::get, Minecraft.getInstance());
    }

    @Override
    public PacketFlow flow() {
        return PacketFlow.CLIENTBOUND;
    }
}
