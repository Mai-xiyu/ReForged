package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;
import org.xiyu.reforged.bridge.NeoForgeEventBusAdapter;

import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;

/**
 * NeoForgeModLoader &mdash; Runtime discovery and loading of NeoForge mods.
 *
 * <h3>How it works:</h3>
 * <ol>
 *   <li>Scans the {@code mods/} folder for JARs containing {@code META-INF/neoforge.mods.toml}
 *       but NOT {@code META-INF/mods.toml} (to avoid interfering with real Forge mods)</li>
 *   <li>Creates a {@link URLClassLoader} parented by the game's classloader,
 *       containing the NeoForge mod JARs</li>
 *   <li>Uses ASM to scan for {@code @net.neoforged.fml.common.Mod} annotated classes</li>
 *   <li>Instantiates each mod class, passing the event bus (wrapped as NeoForge's IEventBus)</li>
 * </ol>
 *
 * <p>This runs during ReForged's {@code @Mod} constructor, which fires before registry events.</p>
 *
 * <p>Heavy lifting is delegated to focused helper classes:
 * {@link NeoJarDiscovery}, {@link NeoModClassLoader}, {@link ModuleAccessOpener},
 * {@link NeoModScanner}, {@link NeoModMetadataParser}, {@link NeoModInstantiator},
 * and {@link EventBusSubscriberRegistrar}.</p>
 */
public final class NeoForgeModLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Object> loadedModInstances = new ArrayList<>();

    /** NeoForge JAR paths that were successfully loaded (for resource pack registration). */
    private static final List<Path> loadedNeoJarPaths = new ArrayList<>();

    /** Stored reference to Forge's mod event bus. */
    private static IEventBus storedModEventBus;

    /**
     * Get the paths of all successfully loaded NeoForge mod JARs.
     * Used by the resource pack injector to register JAR resources.
     */
    public static List<Path> getLoadedNeoJarPaths() {
        return Collections.unmodifiableList(loadedNeoJarPaths);
    }

    /**
     * Dispatch a NeoForge mod-bus event to all NeoForge mod listeners.
     *
     * <p>Since NeoForge's {@code net.neoforged.bus.api.Event} extends Forge's
     * {@code net.minecraftforge.eventbus.api.Event}, NeoForge events can be posted
     * directly on the Forge mod event bus. NeoForge mod handlers registered via
     * {@link NeoForgeEventBusAdapter} will receive them.</p>
     *
     * @param event a NeoForge event instance (must extend Forge's Event through inheritance)
     */
    public static void dispatchNeoForgeModEvent(net.minecraftforge.eventbus.api.Event event) {
        if (storedModEventBus == null) {
            LOGGER.warn("[ReForged] Cannot dispatch NeoForge mod event {} — mod bus not yet initialised",
                    event.getClass().getSimpleName());
            return;
        }
        try {
            storedModEventBus.post(event);
        } catch (Throwable t) {
            LOGGER.error("[ReForged] Failed to dispatch NeoForge mod event {}: {}",
                    event.getClass().getSimpleName(), t.getMessage(), t);
        }
    }

    /**
     * Discover and load all NeoForge mods from the given directory.
     *
     * @param modsDir   the mods directory to scan
     * @param modBus    Forge's mod event bus (from FMLJavaModLoadingContext)
     */
    public static void loadAll(Path modsDir, IEventBus modBus) {
        storedModEventBus = modBus;
        if (!Files.isDirectory(modsDir)) {
            LOGGER.debug("[ReForged] Mods directory not found: {}", modsDir);
            return;
        }

        // Phase 1: Find NeoForge mod JARs (mods/ directory + classpath)
        List<Path> neoJars = new ArrayList<>(NeoJarDiscovery.discoverNeoForgeJars(modsDir));

        // Phase 1.5: Also scan the classpath for NeoForge mod JARs (dev environment)
        List<Path> classpathJars = NeoJarDiscovery.discoverNeoForgeJarsOnClasspath();
        if (!classpathJars.isEmpty()) {
            // Deduplicate by filename
            Set<String> existing = new HashSet<>();
            for (Path p : neoJars) existing.add(p.getFileName().toString());
            for (Path cp : classpathJars) {
                if (existing.add(cp.getFileName().toString())) {
                    neoJars.add(cp);
                    LOGGER.info("[ReForged] Found NeoForge mod on classpath: {}", cp.getFileName());
                }
            }
        }

        if (neoJars.isEmpty()) {
            LOGGER.info("[ReForged] No NeoForge mods found in {} or on classpath", modsDir);
            return;
        }
        LOGGER.info("[ReForged] Found {} NeoForge mod JAR(s): {}", neoJars.size(),
                neoJars.stream().map(p -> p.getFileName().toString()).toList());

        // Phase 1.75: Run compatibility analysis before loading
        try {
            NeoForgeModAnalyzer.analyzeAndReport(neoJars);
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] Mod analysis failed (non-fatal): {}", t.getMessage());
        }

        // Phase 2: Create classloader with all NeoForge JARs
        URLClassLoader neoClassLoader = NeoModClassLoader.createClassLoader(
                neoJars, NeoForgeModLoader.class.getClassLoader());
        if (neoClassLoader == null) return;

        // Phase 2.5: Open game module packages to the URLClassLoader's unnamed module
        // so NeoForge mod classes can access Minecraft/Forge classes across the module boundary.
        ModuleAccessOpener.openGameModulesToClassLoader(neoClassLoader, NeoForgeModLoader.class);

        // Phase 2.75: Process enum extensions from NeoForge mods
        // Must happen BEFORE any mod classes are loaded / initialized
        EnumExtensionHandler.processAll(neoJars, neoClassLoader);

        // Phase 3: Wrap the event bus as NeoForge's IEventBus
        net.neoforged.bus.api.IEventBus busAdapter = NeoForgeEventBusAdapter.wrap(modBus);

        // Make the bus available to NeoForge ModContainer wrappers
        net.neoforged.fml.ModContainer.setGlobalModBus(busAdapter);

        // Phase 3.5: Temporarily unfreeze the root registry so NeoForge mods that
        // directly call Registry.register() on BuiltInRegistries.REGISTRY (e.g. Create's
        // CreateBuiltInRegistries) don't hit "Registry is already frozen".
        Field rootFrozenField = null;
        boolean rootWasFrozen = false;
        try {
            Object rootRegistry = net.minecraft.core.registries.BuiltInRegistries.REGISTRY;
            rootFrozenField = findFrozenField(rootRegistry.getClass());
            if (rootFrozenField != null) {
                rootFrozenField.setAccessible(true);
                rootWasFrozen = rootFrozenField.getBoolean(rootRegistry);
                if (rootWasFrozen) {
                    rootFrozenField.setBoolean(rootRegistry, false);
                    LOGGER.info("[ReForged] Temporarily unfroze root registry for NeoForge mod loading");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[ReForged] Could not unfreeze root registry: {}", e.getMessage());
        }

        // Phase 4: Scan and load each mod, collecting containers for ModList injection
        List<ModListInjector.NeoModContainer> neoContainers = new ArrayList<>();
        List<String> failedMods = new ArrayList<>();
        for (Path jar : neoJars) {
            try {
                int before = loadedModInstances.size();
                loadModsFromJar(jar, neoClassLoader, busAdapter, modBus, neoContainers);
                if (loadedModInstances.size() > before) {
                    // At least one mod from this JAR loaded successfully — track it for resource packs
                    loadedNeoJarPaths.add(jar);
                }
            } catch (Throwable e) {
                String jarName = jar.getFileName().toString();
                failedMods.add(jarName);
                LOGGER.error("[ReForged] Failed to load NeoForge mod from {}", jarName, e);
            }
        }

        // Phase 4.5: Re-freeze the root registry now that NeoForge mod construction is complete
        if (rootWasFrozen && rootFrozenField != null) {
            try {
                rootFrozenField.setBoolean(net.minecraft.core.registries.BuiltInRegistries.REGISTRY, true);
                LOGGER.info("[ReForged] Re-froze root registry after NeoForge mod loading");
            } catch (Exception e) {
                LOGGER.warn("[ReForged] Could not re-freeze root registry: {}", e.getMessage());
            }
        }

        if (!failedMods.isEmpty()) {
            LOGGER.warn("[ReForged] {} NeoForge mod JAR(s) had loading failures: {} — continuing without them",
                    failedMods.size(), failedMods);
        }
        LOGGER.info("[ReForged] Loaded {} NeoForge mod instance(s)", loadedModInstances.size());

        // Phase 5: Register NeoForge mods in Forge's ModList so they appear in the Mods screen
        ModListInjector.inject(neoContainers);

        // Phase 6: Auto-register @EventBusSubscriber classes from NeoForge JARs
        EventBusSubscriberRegistrar.registerAll(neoJars, neoClassLoader, modBus);

        // Phase 7: Load configs registered by NeoForge mods
        // Forge's ConfigTracker loads COMMON/CLIENT configs during early startup, before
        // NeoForge mods are constructed. Configs registered by NeoForge mods through our
        // bridge (ModContainer.registerConfig) are tracked but never loaded. Re-invoking
        // loadConfigs() loads the new configs and fires ModConfigEvent.Loading so mods
        // like Balm can initialize their config system.
        try {
            java.nio.file.Path configDir = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
            net.minecraftforge.fml.config.ConfigTracker.INSTANCE.loadConfigs(
                    net.minecraftforge.fml.config.ModConfig.Type.COMMON, configDir);
            net.minecraftforge.fml.config.ConfigTracker.INSTANCE.loadConfigs(
                    net.minecraftforge.fml.config.ModConfig.Type.CLIENT, configDir);
            LOGGER.info("[ReForged] Re-loaded COMMON and CLIENT configs for NeoForge mod config initialization");
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] Config loading after NeoForge mod init failed: {}", t.getMessage());
        }
    }

    /**
     * Find the "frozen" field in a MappedRegistry (tries official + SRG names).
     */
    private static Field findFrozenField(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (String name : new String[]{"frozen", "f_205845_"}) {
                try {
                    Field f = current.getDeclaredField(name);
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException ignored) {}
            }
            current = current.getSuperclass();
        }
        return null;
    }

    /**
     * Scan a JAR for @Mod annotated classes and instantiate them.
     * Pre-creates containers for each mod and sets them as the active Forge container
     * during construction, so configs register under the correct mod ID.
     * Successfully loaded containers are added to {@code collector} for later ModList injection.
     */
    @SuppressWarnings("removal")
    private static void loadModsFromJar(Path jarPath, URLClassLoader classLoader,
                                         net.neoforged.bus.api.IEventBus busAdapter, IEventBus modBus,
                                         List<ModListInjector.NeoModContainer> collector) throws Exception {
        // Scan for @Mod classes using ASM (without loading classes)
        List<NeoModScanner.ModInfo> modClasses = NeoModScanner.scanForModClasses(jarPath);
        // Parse TOML metadata from the JAR
        Map<String, NeoModMetadataParser.ModMetadata> metadata = NeoModMetadataParser.parseModMetadata(jarPath);

        // Perform a full annotation scan of the JAR so NeoForge mods
        // (e.g. Twilight Forest BeanContext) can find their annotated classes
        // through ModFileScanData.getAnnotatedBy().
        net.minecraftforge.forgespi.language.ModFileScanData jarScanData = NeoModScanner.scanJarAnnotations(jarPath);
        LOGGER.info("[ReForged] Scanned {} annotations and {} classes from {}",
                jarScanData.getAnnotations().size(), jarScanData.getClasses().size(),
                jarPath.getFileName());

        for (NeoModScanner.ModInfo info : modClasses) {
            LOGGER.info("[ReForged] Loading NeoForge mod: '{}' (class: {})", info.modId(), info.className());
            try {
                Class<?> modClass = classLoader.loadClass(info.className());

                // Create NeoModData and Forge container BEFORE construction
                NeoModMetadataParser.ModMetadata meta = metadata.getOrDefault(info.modId(), NeoModMetadataParser.DEFAULT_METADATA);
                ModListInjector.NeoModData neoModData = new ModListInjector.NeoModData(
                        info.modId(),
                        meta.displayName(),
                        meta.version(),
                        meta.description(),
                        meta.license(),
                        meta.logoFile(),
                        null,  // instance not yet available
                        jarScanData,
                        jarPath
                );
                ModListInjector.NeoModContainer neoContainer = ModListInjector.createContainer(neoModData);

                // Pre-register the NeoForge mod file info so ModList.getModFileById()
                // works during static class initialization (before inject() completes)
                var forgeFileInfo = neoContainer.getModInfo().getOwningFile();
                if (forgeFileInfo != null) {
                    var neoFileInfo = net.neoforged.neoforgespi.language.IModFileInfo.wrap(forgeFileInfo);
                    net.neoforged.fml.ModList.registerNeoModFileInfo(info.modId(), neoFileInfo);
                }

                // Swap Forge's active container to our NeoForge container so configs
                // registered during construction use the correct mod ID
                var forgeCtx = net.minecraftforge.fml.ModLoadingContext.get();
                var oldContainer = forgeCtx.getActiveContainer();
                forgeCtx.setActiveContainer(neoContainer);

                // Inject the container into ModList BEFORE construction,
                // so mods that look themselves up (e.g. TwilightForest BeanContext)
                // can find their container during static initialization.
                ModListInjector.inject(List.of(neoContainer));

                Object instance;
                try {
                    instance = NeoModInstantiator.instantiateMod(modClass, busAdapter, modBus);
                } finally {
                    // Restore the original active container
                    forgeCtx.setActiveContainer(oldContainer);
                }

                if (instance != null) {
                    loadedModInstances.add(instance);
                    neoContainer.setModInstance(instance);
                    collector.add(neoContainer);
                    LOGGER.info("[ReForged] Successfully loaded NeoForge mod '{}'", info.modId());
                }
            } catch (Throwable e) {
                // Catch Throwable (not just Exception) to handle ExceptionInInitializerError,
                // NoClassDefFoundError, VerifyError, etc. from NeoForge mod static initializers.
                // We log the failure but continue loading remaining mod classes from this JAR.
                String cause = e.getMessage();
                if (e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null) {
                    cause = e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage();
                }
                LOGGER.error("[ReForged] Failed to load mod class '{}': {} — skipping this mod, continuing",
                        info.className(), cause, e);
            }
        }
    }
}
