package net.neoforged.fml;

import java.util.*;
import java.util.function.Consumer;

/**
 * Proxy for NeoForge's {@code ModList}.
 * Delegates to Forge's {@link net.minecraftforge.fml.ModList}.
 */
public class ModList {

    /**
     * Get the active mod list.
     */
    public static ModList get() {
        return new ModList();
    }

    /**
     * Check if a mod is loaded.
     */
    public boolean isLoaded(String modid) {
        return net.minecraftforge.fml.ModList.get().isLoaded(modid);
    }

    /**
     * Get a mod container by ID.
     */
    public Optional<? extends net.minecraftforge.fml.ModContainer> getModContainerById(String modid) {
        return net.minecraftforge.fml.ModList.get().getModContainerById(modid);
    }

    /**
     * Get all mod IDs.
     */
    public List<String> getMods() {
        return net.minecraftforge.fml.ModList.get().getMods().stream()
                .map(info -> info.getModId())
                .toList();
    }

    /**
     * Run for each mod container.
     */
    public void forEachModContainer(java.util.function.BiConsumer<String, net.minecraftforge.fml.ModContainer> consumer) {
        net.minecraftforge.fml.ModList.get().forEachModContainer(consumer);
    }
}
