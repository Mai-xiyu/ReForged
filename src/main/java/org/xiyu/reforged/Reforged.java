package org.xiyu.reforged;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.xiyu.reforged.core.NeoForgeModLoader;
import org.slf4j.Logger;

import java.nio.file.*;
import java.util.*;

/**
 * ReForged — NeoForge Compatibility Bridge for Forge 1.21.1
 *
 * <p>Drop this mod + NeoForge mods into .minecraft/mods/. ReForged will:
 * <ol>
 *     <li>Scan mods/ for NeoForge JARs (those with neoforge.mods.toml)</li>
 *     <li>Load their classes via a custom ClassLoader</li>
 *     <li>Instantiate their @Mod classes with a bridged event bus</li>
 *     <li>Register their resources (models, textures, sounds, data) as resource packs</li>
 * </ol>
 * No JAR modification needed — everything happens at runtime.</p>
 */
@Mod(Reforged.MODID)
public class Reforged {

    public static final String MODID = "reforged";
    public static final String VERSION = "1.0.0";
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Cached NIO FileSystems for NeoForge JARs — kept alive for the game session. */
    private static final List<FileSystem> jarFileSystems = new ArrayList<>();

    public Reforged(FMLJavaModLoadingContext context) {
        LOGGER.info("========================================");
        LOGGER.info(" ReForged v{} — NeoForge Compatibility Bridge", VERSION);
        LOGGER.info("========================================");

        // ── 1. Discover the mods directory ───────────────────
        Path modsDir = FMLPaths.MODSDIR.get();
        LOGGER.info("[ReForged] Mods directory: {}", modsDir);

        // ── 2. Load NeoForge mods at runtime ─────────────────
        NeoForgeModLoader.loadAll(modsDir, context.getModEventBus());

        // ── 3. Register event listeners ──────────────────────
        context.getModEventBus().addListener(this::commonSetup);
        context.getModEventBus().addListener(this::addPackFinders);

        LOGGER.info("[ReForged] Bootstrap complete");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[ReForged] Common setup phase — NeoForge bridge active");
    }

    /**
     * Register NeoForge mod JARs as resource packs so their assets/ and data/
     * are available to Forge's resource system (models, textures, sounds, recipes, etc.).
     *
     * <p>This event fires during {@link net.minecraft.server.packs.repository.PackRepository}
     * construction, which happens AFTER mod construction, so the NeoForge JARs have
     * already been discovered by {@link NeoForgeModLoader#loadAll}.</p>
     */
    private void addPackFinders(final AddPackFindersEvent event) {
        List<Path> neoJars = NeoForgeModLoader.getLoadedNeoJarPaths();
        if (neoJars.isEmpty()) return;

        LOGGER.info("[ReForged] Registering {} NeoForge JAR(s) as {} packs",
                neoJars.size(), event.getPackType());

        for (Path jarPath : neoJars) {
            try {
                // Open the JAR as a NIO FileSystem so PathPackResources can read from it.
                // We cache the FileSystem to keep it alive for the game session.
                FileSystem jarFs;
                try {
                    jarFs = FileSystems.newFileSystem(jarPath, (ClassLoader) null);
                } catch (FileSystemAlreadyExistsException e) {
                    // Already opened (e.g. CLIENT_RESOURCES event fired before SERVER_DATA)
                    jarFs = FileSystems.getFileSystem(jarPath.toUri());
                }
                jarFileSystems.add(jarFs);

                Path root = jarFs.getPath("/");
                var supplier = new PathPackResources.PathResourcesSupplier(root);
                String jarName = jarPath.getFileName().toString();
                String packId = "neomod:" + jarName.replace(".jar", "");

                var info = new PackLocationInfo(
                        packId,
                        Component.literal("[NeoForge] " + jarName),
                        PackSource.BUILT_IN,
                        Optional.empty()
                );

                Pack pack = Pack.readMetaAndCreate(
                        info,
                        supplier,
                        event.getPackType(),
                        new PackSelectionConfig(true, Pack.Position.BOTTOM, false)
                );

                if (pack != null) {
                    event.addRepositorySource(consumer -> consumer.accept(pack));
                    LOGGER.info("[ReForged] ✓ Registered resource pack '{}' for {}",
                            packId, event.getPackType());
                } else {
                    LOGGER.warn("[ReForged] ✗ Could not read pack metadata from '{}' for {} — " +
                            "JAR may be missing pack.mcmeta", jarName, event.getPackType());
                }
            } catch (Exception e) {
                LOGGER.error("[ReForged] Failed to register resource pack for {}",
                        jarPath.getFileName(), e);
            }
        }
    }
}
