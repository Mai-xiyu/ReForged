package net.neoforged.neoforge.registries.callback;

/** Proxy: NeoForge's AddCallback for registry events */
@FunctionalInterface
public interface AddCallback<T> {
    void onAdd(Object owner, int id, net.minecraft.resources.ResourceKey<?> key, T value);
}
