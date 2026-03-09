package org.xiyu.reforged.mixin;

import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.neoforged.neoforge.common.extensions.IServerCommonPacketListenerExtension;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.connection.ConnectionUtils;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.xiyu.reforged.shim.network.PayloadChannelRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin implements IServerCommonPacketListenerExtension {

    public void send(CustomPacketPayload payload) {
        PayloadChannelRegistry.sendViaConnection(((ServerCommonPacketListenerImpl) (Object) this).getConnection(), payload);
    }

    public void send(CustomPacketPayload payload, @Nullable PacketSendListener listener) {
        PayloadChannelRegistry.sendViaConnection(((ServerCommonPacketListenerImpl) (Object) this).getConnection(), payload);
        if (listener != null) {
            listener.onSuccess();
        }
    }

    public net.minecraft.util.thread.ReentrantBlockableEventLoop<?> getMainThreadEventLoop() {
		return ServerLifecycleHooks.getCurrentServer();
    }

    public ConnectionType getConnectionType() {
        return ConnectionUtils.getConnectionType(((ServerCommonPacketListenerImpl) (Object) this).getConnection());
    }
}
