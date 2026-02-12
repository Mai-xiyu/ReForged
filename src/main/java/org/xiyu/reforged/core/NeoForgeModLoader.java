package org.xiyu.reforged.core;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.xiyu.reforged.bridge.NeoForgeEventBusAdapter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/**
 * NeoForgeModLoader — Runtime discovery and loading of NeoForge mods.
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
 */
public final class NeoForgeModLoader {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NEO_MOD_ANNOTATION = "Lnet/neoforged/fml/common/Mod;";
    private static final List<Object> loadedModInstances = new ArrayList<>();

    /** NeoForge JAR paths that were successfully loaded (for resource pack registration). */
    private static final List<Path> loadedNeoJarPaths = new ArrayList<>();

    /** Default metadata used when TOML parsing fails or a modId is not found in TOML. */
    private static final ModMetadata DEFAULT_METADATA =
            new ModMetadata("Unknown Mod", "1.0.0", "", "Unknown", null);

    private record ModMetadata(String displayName, String version,
                                String description, String license, String logoFile) {}

    /**
     * Get the paths of all successfully loaded NeoForge mod JARs.
     * Used by the resource pack injector to register JAR resources.
     */
    public static List<Path> getLoadedNeoJarPaths() {
        return Collections.unmodifiableList(loadedNeoJarPaths);
    }

    /**
     * Discover and load all NeoForge mods from the given directory.
     *
     * @param modsDir   the mods directory to scan
     * @param modBus    Forge's mod event bus (from FMLJavaModLoadingContext)
     */
    public static void loadAll(Path modsDir, IEventBus modBus) {
        if (!Files.isDirectory(modsDir)) {
            LOGGER.debug("[ReForged] Mods directory not found: {}", modsDir);
            return;
        }

        // Phase 1: Find NeoForge mod JARs
        List<Path> neoJars = discoverNeoForgeJars(modsDir);
        if (neoJars.isEmpty()) {
            LOGGER.info("[ReForged] No NeoForge mods found in {}", modsDir);
            return;
        }
        LOGGER.info("[ReForged] Found {} NeoForge mod JAR(s): {}", neoJars.size(),
                neoJars.stream().map(p -> p.getFileName().toString()).toList());

        // Phase 2: Create classloader with all NeoForge JARs
        URLClassLoader neoClassLoader = createClassLoader(neoJars);
        if (neoClassLoader == null) return;

        // Phase 3: Wrap the event bus as NeoForge's IEventBus
        net.neoforged.bus.api.IEventBus busAdapter = NeoForgeEventBusAdapter.wrap(modBus);

        // Phase 4: Scan and load each mod, collecting containers for ModList injection
        List<ModListInjector.NeoModContainer> neoContainers = new ArrayList<>();
        for (Path jar : neoJars) {
            try {
                int before = loadedModInstances.size();
                loadModsFromJar(jar, neoClassLoader, busAdapter, modBus, neoContainers);
                if (loadedModInstances.size() > before) {
                    // At least one mod from this JAR loaded successfully — track it for resource packs
                    loadedNeoJarPaths.add(jar);
                }
            } catch (Exception e) {
                LOGGER.error("[ReForged] Failed to load NeoForge mod from {}", jar.getFileName(), e);
            }
        }

        LOGGER.info("[ReForged] Loaded {} NeoForge mod instance(s)", loadedModInstances.size());

        // Phase 5: Register NeoForge mods in Forge's ModList so they appear in the Mods screen
        ModListInjector.inject(neoContainers);

        // Phase 6: Auto-register @EventBusSubscriber classes from NeoForge JARs
        registerEventBusSubscribers(neoJars, neoClassLoader, modBus);
    }

    /**
     * Find JARs in modsDir that have neoforge.mods.toml but not mods.toml.
     */
    private static List<Path> discoverNeoForgeJars(Path modsDir) {
        List<Path> result = new ArrayList<>();
        try (var stream = Files.list(modsDir)) {
            for (Path path : stream.filter(p -> p.toString().endsWith(".jar")).toList()) {
                try (JarFile jar = new JarFile(path.toFile())) {
                    boolean hasNeo = jar.getJarEntry("META-INF/neoforge.mods.toml") != null;
                    boolean hasForge = jar.getJarEntry("META-INF/mods.toml") != null;
                    if (hasNeo && !hasForge) {
                        result.add(path);
                    }
                } catch (Exception e) {
                    LOGGER.debug("[ReForged] Skipping {}: {}", path.getFileName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[ReForged] Error scanning mods directory", e);
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════
    //  @EventBusSubscriber auto-registration
    // ═══════════════════════════════════════════════════════════

    private static final String EVENT_BUS_SUBSCRIBER_DESC = "Lnet/neoforged/fml/common/EventBusSubscriber;";

    /**
     * Scan all NeoForge mod JARs for classes annotated with {@code @EventBusSubscriber}
     * and register their {@code @SubscribeEvent} methods on the appropriate event bus.
     *
     * <ul>
     *   <li>{@code bus = MOD} → registers on Forge's mod event bus</li>
     *   <li>{@code bus = GAME} (default) → registers on {@code MinecraftForge.EVENT_BUS}</li>
     * </ul>
     *
     * <p>Respects the {@code value} (Dist) filter: CLIENT-only classes are skipped on servers.</p>
     */
    private static void registerEventBusSubscribers(List<Path> jars, URLClassLoader classLoader,
                                                     IEventBus modBus) {
        boolean isClient = net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient();

        for (Path jarPath : jars) {
            try {
                List<SubscriberInfo> subscribers = scanForEventBusSubscribers(jarPath);
                for (SubscriberInfo info : subscribers) {
                    // Check Dist filter
                    if (!info.dists.isEmpty()) {
                        boolean matches = info.dists.stream().anyMatch(d ->
                                ("CLIENT".equals(d) && isClient) ||
                                ("DEDICATED_SERVER".equals(d) && !isClient));
                        if (!matches) {
                            LOGGER.debug("[ReForged] Skipping {} — Dist filter mismatch", info.className);
                            continue;
                        }
                    }

                    try {
                        Class<?> subscriberClass = classLoader.loadClass(info.className);

                        // Determine target bus
                        net.minecraftforge.eventbus.api.IEventBus targetBus;
                        if ("MOD".equals(info.bus)) {
                            targetBus = modBus;
                        } else {
                            // GAME / FORGE → MinecraftForge.EVENT_BUS
                            targetBus = net.minecraftforge.common.MinecraftForge.EVENT_BUS;
                        }

                        // Register the class (static methods with @SubscribeEvent)
                        NeoForgeEventBusAdapter.handleRegister(targetBus, subscriberClass);

                        LOGGER.info("[ReForged] ✓ Auto-registered @EventBusSubscriber: {} (bus={}, dist={})",
                                subscriberClass.getSimpleName(), info.bus, info.dists);
                    } catch (Throwable t) {
                        // Catch Throwable (not just Exception) — NoClassDefFoundError can
                        // propagate from Class.getMethods/getDeclaredMethods when the class
                        // references NeoForge event types without shims.
                        LOGGER.warn("[ReForged] Skipping subscriber class {} — {}",
                                info.className, t.getMessage());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[ReForged] Failed to scan {} for @EventBusSubscriber", jarPath.getFileName(), e);
            }
        }
    }

    /**
     * ASM-scan a JAR for classes annotated with {@code @net.neoforged.fml.common.EventBusSubscriber}.
     */
    private static List<SubscriberInfo> scanForEventBusSubscribers(Path jarPath) {
        List<SubscriberInfo> result = new ArrayList<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                if (entry.getName().startsWith("META-INF/")) continue;

                try (InputStream is = jar.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(is);
                    EventBusSubscriberScanner scanner = new EventBusSubscriberScanner();
                    reader.accept(scanner, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);

                    if (scanner.isSubscriber) {
                        String className = entry.getName().replace('/', '.').replace(".class", "");
                        result.add(new SubscriberInfo(className, scanner.bus, scanner.dists));
                    }
                } catch (Exception e) {
                    // Skip unreadable class files
                }
            }
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to scan JAR for subscribers: {}", jarPath.getFileName(), e);
        }
        return result;
    }

    private record SubscriberInfo(String className, String bus, List<String> dists) {}

    /**
     * ASM visitor that detects {@code @EventBusSubscriber} and extracts bus + value (Dist) fields.
     */
    private static class EventBusSubscriberScanner extends ClassVisitor {
        boolean isSubscriber = false;
        String bus = "GAME";     // default bus
        List<String> dists = new ArrayList<>();

        EventBusSubscriberScanner() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (EVENT_BUS_SUBSCRIBER_DESC.equals(descriptor)) {
                isSubscriber = true;
                return new AnnotationVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitEnum(String name, String enumDesc, String value) {
                        if ("bus".equals(name)) {
                            bus = value;  // "MOD" or "GAME"
                        }
                    }

                    @Override
                    public AnnotationVisitor visitArray(String name) {
                        if ("value".equals(name)) {
                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override
                                public void visitEnum(String n, String desc, String value) {
                                    dists.add(value);  // "CLIENT" or "DEDICATED_SERVER"
                                }
                            };
                        }
                        return super.visitArray(name);
                    }
                };
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }

    /**
     * Create a URLClassLoader for the NeoForge mod JARs.
     * Parent is the current classloader (game + Forge + ReForged).
     */
    private static URLClassLoader createClassLoader(List<Path> jars) {
        try {
            URL[] urls = new URL[jars.size()];
            for (int i = 0; i < jars.size(); i++) {
                urls[i] = jars.get(i).toUri().toURL();
            }
            return new URLClassLoader(urls, NeoForgeModLoader.class.getClassLoader());
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to create classloader", e);
            return null;
        }
    }

    /**
     * Scan a JAR for @Mod annotated classes and instantiate them.
     * Pre-creates containers for each mod and sets them as the active Forge container
     * during construction, so configs register under the correct mod ID.
     * Successfully loaded containers are added to {@code collector} for later ModList injection.
     */
    private static void loadModsFromJar(Path jarPath, URLClassLoader classLoader,
                                         net.neoforged.bus.api.IEventBus busAdapter, IEventBus modBus,
                                         List<ModListInjector.NeoModContainer> collector) throws Exception {
        // Scan for @Mod classes using ASM (without loading classes)
        List<ModInfo> modClasses = scanForModClasses(jarPath);
        // Parse TOML metadata from the JAR
        Map<String, ModMetadata> metadata = parseModMetadata(jarPath);

        for (ModInfo info : modClasses) {
            LOGGER.info("[ReForged] Loading NeoForge mod: '{}' (class: {})", info.modId, info.className);
            try {
                Class<?> modClass = classLoader.loadClass(info.className);

                // Create NeoModData and Forge container BEFORE construction
                ModMetadata meta = metadata.getOrDefault(info.modId, DEFAULT_METADATA);
                ModListInjector.NeoModData neoModData = new ModListInjector.NeoModData(
                        info.modId,
                        meta.displayName(),
                        meta.version(),
                        meta.description(),
                        meta.license(),
                        meta.logoFile(),
                        null  // instance not yet available
                );
                ModListInjector.NeoModContainer neoContainer = ModListInjector.createContainer(neoModData);

                // Swap Forge's active container to our NeoForge container so configs
                // registered during construction use the correct mod ID
                var forgeCtx = net.minecraftforge.fml.ModLoadingContext.get();
                var oldContainer = forgeCtx.getActiveContainer();
                forgeCtx.setActiveContainer(neoContainer);

                Object instance;
                try {
                    instance = instantiateMod(modClass, busAdapter, modBus);
                } finally {
                    // Restore the original active container
                    forgeCtx.setActiveContainer(oldContainer);
                }

                if (instance != null) {
                    loadedModInstances.add(instance);
                    neoContainer.setModInstance(instance);
                    collector.add(neoContainer);
                    LOGGER.info("[ReForged] ✓ Successfully loaded NeoForge mod '{}'", info.modId);
                }
            } catch (Exception e) {
                LOGGER.error("[ReForged] ✗ Failed to load mod class '{}': {}", info.className, e.getMessage(), e);
            }
        }
    }

    /**
     * Use ASM to scan a JAR for classes annotated with @net.neoforged.fml.common.Mod.
     */
    private static List<ModInfo> scanForModClasses(Path jarPath) {
        List<ModInfo> result = new ArrayList<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                if (entry.getName().startsWith("META-INF/")) continue;

                try (InputStream is = jar.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(is);
                    ModAnnotationScanner scanner = new ModAnnotationScanner();
                    reader.accept(scanner, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);

                    if (scanner.modId != null) {
                        String className = entry.getName()
                                .replace('/', '.')
                                .replace(".class", "");
                        result.add(new ModInfo(scanner.modId, className));
                    }
                } catch (Exception e) {
                    // Skip unreadable class files
                }
            }
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to scan JAR: {}", jarPath.getFileName(), e);
        }
        return result;
    }

    /**
     * Instantiate a NeoForge mod class, trying multiple constructor patterns.
     */
    private static Object instantiateMod(Class<?> modClass, net.neoforged.bus.api.IEventBus busAdapter,
                                          IEventBus modBus) throws Exception {
        // Pattern 1: (net.neoforged.bus.api.IEventBus)
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor(net.neoforged.bus.api.IEventBus.class);
            ctor.setAccessible(true);
            return ctor.newInstance(busAdapter);
        } catch (NoSuchMethodException ignored) {}

        // Pattern 2: (net.minecraftforge.eventbus.api.IEventBus) — some mods use Forge's type directly
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor(IEventBus.class);
            ctor.setAccessible(true);
            return ctor.newInstance(modBus);
        } catch (NoSuchMethodException ignored) {}

        // Pattern 3: (net.neoforged.bus.api.IEventBus, net.neoforged.fml.ModContainer)
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor(
                    net.neoforged.bus.api.IEventBus.class,
                    net.neoforged.fml.ModContainer.class);
            ctor.setAccessible(true);
            // Create a dummy ModContainer
            net.neoforged.fml.ModContainer container =
                    new net.neoforged.fml.ModContainer(
                            net.minecraftforge.fml.ModLoadingContext.get().getActiveContainer());
            return ctor.newInstance(busAdapter, container);
        } catch (NoSuchMethodException ignored) {}

        // Pattern 4: no-arg constructor
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException ignored) {}

        LOGGER.error("[ReForged] No compatible constructor found for {}", modClass.getName());
        return null;
    }

    /**
     * ASM visitor that scans for @Mod annotation and extracts the modId.
     */
    private static class ModAnnotationScanner extends ClassVisitor {
        String modId = null;

        ModAnnotationScanner() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (NEO_MOD_ANNOTATION.equals(descriptor)) {
                return new AnnotationVisitor(Opcodes.ASM9) {
                    @Override
                    public void visit(String name, Object value) {
                        if ("value".equals(name) && value instanceof String s) {
                            modId = s;
                        }
                    }
                };
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }

    private record ModInfo(String modId, String className) {}

    /**
     * Parse {@code META-INF/neoforge.mods.toml} from a JAR to extract mod display metadata.
     * Falls back gracefully if TOML is missing or unparseable.
     *
     * @return map of modId → metadata; empty map on failure
     */
    private static Map<String, ModMetadata> parseModMetadata(Path jarPath) {
        Map<String, ModMetadata> result = new HashMap<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            // Resolve JAR version from manifest (for ${file.jarVersion} substitution)
            String jarVersion = "1.0.0";
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
                String implVer = manifest.getMainAttributes().getValue("Implementation-Version");
                if (implVer != null && !implVer.isEmpty()) jarVersion = implVer;
            }

            JarEntry tomlEntry = jar.getJarEntry("META-INF/neoforge.mods.toml");
            if (tomlEntry == null) return result;

            try (InputStream is = jar.getInputStream(tomlEntry)) {
                var config = new TomlParser().parse(new InputStreamReader(is, StandardCharsets.UTF_8));
                String license = config.getOrElse("license", "Unknown");

                Object modsObj = config.get("mods");
                if (modsObj instanceof List<?> modsList) {
                    for (Object entry : modsList) {
                        if (!(entry instanceof UnmodifiableConfig modConf)) continue;
                        String modId = modConf.get("modId");
                        if (modId == null) continue;

                        String displayName = modConf.getOrElse("displayName", modId);
                        String version = modConf.getOrElse("version", jarVersion);
                        String description = modConf.getOrElse("description", "");
                        String logoFile = modConf.get("logoFile");

                        // Resolve template variables like ${file.jarVersion}
                        if (version.contains("${")) version = jarVersion;

                        result.put(modId, new ModMetadata(displayName, version, description, license, logoFile));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("[ReForged] Could not parse TOML metadata from {}: {}",
                    jarPath.getFileName(), e.getMessage());
        }
        return result;
    }
}
