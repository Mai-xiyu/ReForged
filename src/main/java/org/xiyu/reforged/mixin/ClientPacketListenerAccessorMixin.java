package org.xiyu.reforged.mixin;

import net.createmod.ponder.mixin.client.accessor.ClientPacketListenerAccessor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Makes ClientPacketListener implement Ponder's ClientPacketListenerAccessor,
 * exposing the serverChunkRadius field.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerAccessorMixin implements ClientPacketListenerAccessor {

    @Shadow
    public int serverChunkRadius;

    @Override
    public int catnip$getServerChunkRadius() {
        return this.serverChunkRadius;
    }
}
