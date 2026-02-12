package net.neoforged.neoforge.registries;

import net.minecraftforge.eventbus.api.Event;

/** Proxy: NeoForge's RegisterEvent */
public class RegisterEvent extends Event {
    @FunctionalInterface
    public interface RegisterHelper<T> {
        void register(net.minecraft.resources.ResourceLocation id, T value);
    }
}
