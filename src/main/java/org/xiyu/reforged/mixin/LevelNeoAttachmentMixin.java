package org.xiyu.reforged.mixin;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.xiyu.reforged.bridge.NeoAttachmentHolderBridge;

import java.util.Optional;
import java.util.function.Supplier;

@Mixin(Level.class)
public class LevelNeoAttachmentMixin implements IAttachmentHolder, NeoAttachmentHolderBridge {

    @Unique
    private final AttachmentHolder.AsField reforged$neoAttachmentHolder = new AttachmentHolder.AsField(this);

    @Override
    public boolean hasAttachments() {
        return reforged$neoAttachmentHolder.hasAttachments();
    }

    @Override
    public boolean hasData(AttachmentType<?> type) {
        return reforged$neoAttachmentHolder.hasData(type);
    }

    @Override
    public <T> T getData(AttachmentType<T> type) {
        return reforged$neoAttachmentHolder.getData(type);
    }

    @Override
    public <T> T setData(AttachmentType<T> type, T data) {
        return reforged$neoAttachmentHolder.setData(type, data);
    }

    @Override
    public <T> T removeData(AttachmentType<T> type) {
        return reforged$neoAttachmentHolder.removeData(type);
    }

    @Override
    public <T> Optional<T> getExistingData(AttachmentType<T> type) {
        return IAttachmentHolder.super.getExistingData(type);
    }

    public <T> Optional<T> getExistingData(Supplier<AttachmentType<T>> type) {
        return getExistingData(type.get());
    }

    @Override
    public AttachmentHolder.AsField reforged$getNeoAttachmentHolder() {
        return reforged$neoAttachmentHolder;
    }
}