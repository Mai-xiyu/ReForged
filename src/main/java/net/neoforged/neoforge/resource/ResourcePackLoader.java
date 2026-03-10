package net.neoforged.neoforge.resource;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utility for loading resource packs from mods.
 * In the Forge shim, this delegates to Forge's resource pack system.
 */
public class ResourcePackLoader {
    private ResourcePackLoader() {}

    private static final List<Pack> modPacks = new ArrayList<>();

    /**
     * Register a pack to be added by mods.
     */
    public static void registerPack(Pack pack) {
        modPacks.add(pack);
    }

    /**
     * Get all mod-registered resource packs.
     */
    public static List<Pack> getModPacks() {
        return List.copyOf(modPacks);
    }
}
