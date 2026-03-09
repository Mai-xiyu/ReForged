package net.neoforged.neoforge.attachment;

import net.minecraft.server.level.ServerPlayer;

public interface AttachmentSyncHandler<T> {
    default boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        return true;
    }
}