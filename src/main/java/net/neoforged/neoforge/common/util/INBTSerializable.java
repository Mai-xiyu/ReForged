package net.neoforged.neoforge.common.util;

import net.minecraft.nbt.Tag;

/** Proxy: NeoForge's INBTSerializable */
public interface INBTSerializable<T extends Tag> {
    T serializeNBT();
    void deserializeNBT(T nbt);
}
