package net.neoforged.fml.loading;

import java.nio.file.Path;

/**
 * Proxy: NeoForge's FMLPaths → delegates to Forge's FMLPaths.
 */
public final class FMLPaths {
    public static final FMLPaths GAMEDIR = new FMLPaths(net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get());
    public static final FMLPaths CONFIGDIR = new FMLPaths(net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
    public static final FMLPaths MODSDIR = new FMLPaths(net.minecraftforge.fml.loading.FMLPaths.MODSDIR.get());

    private final Path path;

    private FMLPaths(Path path) { this.path = path; }

    public Path get() { return path; }
}
