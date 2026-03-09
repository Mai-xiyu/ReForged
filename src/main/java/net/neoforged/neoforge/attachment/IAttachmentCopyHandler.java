package net.neoforged.neoforge.attachment;

import net.minecraft.core.HolderLookup;

@FunctionalInterface
public interface IAttachmentCopyHandler<T> {
    T copy(T attachment, IAttachmentHolder holder, HolderLookup.Provider provider);
}