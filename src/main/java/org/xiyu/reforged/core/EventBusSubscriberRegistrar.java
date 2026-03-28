package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.xiyu.reforged.bridge.NeoForgeEventBusAdapter;

import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scans NeoForge mod JARs for classes annotated with {@code @EventBusSubscriber}
 * and registers their {@code @SubscribeEvent} methods on the appropriate event bus.
 *
 * <ul>
 *   <li>{@code bus = MOD} → registers on Forge's mod event bus</li>
 *   <li>{@code bus = GAME} (default) → registers on {@code MinecraftForge.EVENT_BUS}</li>
 * </ul>
 *
 * <p>Respects the {@code value} (Dist) filter: CLIENT-only classes are skipped on servers.</p>
 */
public final class EventBusSubscriberRegistrar {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String EVENT_BUS_SUBSCRIBER_DESC = "Lnet/neoforged/fml/common/EventBusSubscriber;";

    private EventBusSubscriberRegistrar() {}

    /**
     * Scan all NeoForge mod JARs for @EventBusSubscriber classes and register them.
     */
    public static void registerAll(List<Path> jars, URLClassLoader classLoader, IEventBus modBus) {
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

    /** Information about a discovered @EventBusSubscriber class. */
    record SubscriberInfo(String className, String bus, List<String> dists) {}

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
}
