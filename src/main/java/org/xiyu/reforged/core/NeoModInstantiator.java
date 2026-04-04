package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;
import org.xiyu.reforged.bridge.NeoForgeEventBusAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Instantiates NeoForge mod classes, trying multiple constructor patterns.
 *
 * <p>Uses both exact type matching (fast path) and name-based fallback
 * (handles classloader mismatches) to support the various constructor signatures
 * used by NeoForge mods.</p>
 *
 * <h3>Supported constructor patterns:</h3>
 * <ol>
 *   <li>{@code (net.neoforged.bus.api.IEventBus)}</li>
 *   <li>{@code (net.minecraftforge.eventbus.api.IEventBus)}</li>
 *   <li>{@code (IEventBus, ModContainer)}</li>
 *   <li>{@code (ModContainer, IEventBus)} — reversed order</li>
 *   <li>{@code (IEventBus, Dist)}</li>
 *   <li>{@code ()} — no-arg</li>
 * </ol>
 */
public final class NeoModInstantiator {

    private static final Logger LOGGER = LogUtils.getLogger();

    private NeoModInstantiator() {}

    /**
     * Instantiate a NeoForge mod class, trying multiple constructor patterns.
     *
     * @param modClass   the mod class to instantiate
     * @param busAdapter the NeoForge IEventBus adapter
     * @param modBus     the Forge mod event bus
     * @return the mod instance, or null if no compatible constructor was found
     */
    @SuppressWarnings("removal")
    public static Object instantiateMod(Class<?> modClass, net.neoforged.bus.api.IEventBus busAdapter,
                                         IEventBus modBus) throws Exception {
        // === Phase 1: Exact type matching (fast path) ===

        // Pattern 1: (net.neoforged.bus.api.IEventBus)
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor(net.neoforged.bus.api.IEventBus.class);
            ctor.setAccessible(true);
            LOGGER.debug("[ReForged] Matched constructor pattern 1 (IEventBus) for {}", modClass.getName());
            return ctor.newInstance(busAdapter);
        } catch (NoSuchMethodException ignored) {}

        // Pattern 2: (net.minecraftforge.eventbus.api.IEventBus) — some mods use Forge's type directly
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor(IEventBus.class);
            ctor.setAccessible(true);
            LOGGER.debug("[ReForged] Matched constructor pattern 2 (Forge IEventBus) for {}", modClass.getName());
            return ctor.newInstance(modBus);
        } catch (NoSuchMethodException ignored) {}

        // Pattern 3: (net.neoforged.bus.api.IEventBus, net.neoforged.fml.ModContainer)
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor(
                    net.neoforged.bus.api.IEventBus.class,
                    net.neoforged.fml.ModContainer.class);
            ctor.setAccessible(true);
            net.neoforged.fml.ModContainer container =
                    new net.neoforged.fml.ModContainer(
                            net.minecraftforge.fml.ModLoadingContext.get().getActiveContainer());
            LOGGER.debug("[ReForged] Matched constructor pattern 3 (IEventBus, ModContainer) for {}", modClass.getName());
            return ctor.newInstance(busAdapter, container);
        } catch (NoSuchMethodException ignored) {}

        // Pattern 3b: (net.neoforged.fml.ModContainer, net.neoforged.bus.api.IEventBus) — reversed order
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor(
                    net.neoforged.fml.ModContainer.class,
                    net.neoforged.bus.api.IEventBus.class);
            ctor.setAccessible(true);
            net.neoforged.fml.ModContainer container =
                    new net.neoforged.fml.ModContainer(
                            net.minecraftforge.fml.ModLoadingContext.get().getActiveContainer());
            LOGGER.debug("[ReForged] Matched constructor pattern 3b (ModContainer, IEventBus) for {}", modClass.getName());
            return ctor.newInstance(container, busAdapter);
        } catch (NoSuchMethodException ignored) {}

            // Pattern 3c: (net.minecraftforge.eventbus.api.IEventBus, net.minecraftforge.fml.ModContainer)
            try {
                Constructor<?> ctor = modClass.getDeclaredConstructor(
                    IEventBus.class,
                    net.minecraftforge.fml.ModContainer.class);
                ctor.setAccessible(true);
                net.minecraftforge.fml.ModContainer forgeContainer =
                    net.minecraftforge.fml.ModLoadingContext.get().getActiveContainer();
                LOGGER.debug("[ReForged] Matched constructor pattern 3c (Forge IEventBus, Forge ModContainer) for {}", modClass.getName());
                return ctor.newInstance(modBus, forgeContainer);
            } catch (NoSuchMethodException ignored) {}

            // Pattern 3d: (net.minecraftforge.fml.ModContainer, net.minecraftforge.eventbus.api.IEventBus) — reversed order
            try {
                Constructor<?> ctor = modClass.getDeclaredConstructor(
                    net.minecraftforge.fml.ModContainer.class,
                    IEventBus.class);
                ctor.setAccessible(true);
                net.minecraftforge.fml.ModContainer forgeContainer =
                    net.minecraftforge.fml.ModLoadingContext.get().getActiveContainer();
                LOGGER.debug("[ReForged] Matched constructor pattern 3d (Forge ModContainer, Forge IEventBus) for {}", modClass.getName());
                return ctor.newInstance(forgeContainer, modBus);
            } catch (NoSuchMethodException ignored) {}

        // Pattern 4: (net.neoforged.bus.api.IEventBus, net.neoforged.api.distmarker.Dist)
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor(
                    net.neoforged.bus.api.IEventBus.class,
                    net.neoforged.api.distmarker.Dist.class);
            ctor.setAccessible(true);
            net.neoforged.api.distmarker.Dist currentDist =
                    net.neoforged.fml.loading.FMLEnvironment.dist;
            LOGGER.debug("[ReForged] Matched constructor pattern 4 (IEventBus, Dist) for {}", modClass.getName());
            return ctor.newInstance(busAdapter, currentDist);
        } catch (NoSuchMethodException ignored) {}

        // Pattern 5: no-arg constructor
        try {
            Constructor<?> ctor = modClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            LOGGER.debug("[ReForged] Matched constructor pattern 5 (no-arg) for {}", modClass.getName());
            return ctor.newInstance();
        } catch (NoSuchMethodException ignored) {}

        // === Phase 2: Name-based fallback (handles classloader mismatch) ===
        return tryNameBasedFallback(modClass, busAdapter, modBus);
    }

    /**
     * Fallback instantiation using parameter type names instead of identity comparison.
     * Handles the case where constructor parameter types are loaded by a different classloader.
     */
    @SuppressWarnings("removal")
    private static Object tryNameBasedFallback(Class<?> modClass, net.neoforged.bus.api.IEventBus busAdapter,
                                                IEventBus modBus) throws Exception {
        LOGGER.warn("[ReForged] Exact type matching failed for {}. Attempting name-based fallback...", modClass.getName());

        // Log all available constructors for diagnostics
        Constructor<?>[] allCtors = modClass.getDeclaredConstructors();
        for (Constructor<?> c : allCtors) {
            StringBuilder sb = new StringBuilder("  Constructor(");
            Class<?>[] params = c.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(params[i].getName());
                sb.append(" [loader=").append(params[i].getClassLoader()).append("]");
            }
            sb.append(")");
            LOGGER.warn("[ReForged]   Available: {}", sb);
        }
        LOGGER.warn("[ReForged]   busAdapter type: {} [loader={}]",
                busAdapter.getClass().getName(), busAdapter.getClass().getClassLoader());

        // Try name-based matching for known patterns.
        for (Constructor<?> ctor : allCtors) {
            Class<?>[] params = ctor.getParameterTypes();

            // Name-match: (IEventBus)
            if (params.length == 1 && params[0].getName().equals("net.neoforged.bus.api.IEventBus")) {
                ctor.setAccessible(true);
                Object adaptedBus = adaptProxyForClassLoader(busAdapter, params[0], modClass.getClassLoader(), modBus);
                LOGGER.info("[ReForged] Name-based fallback matched (IEventBus) for {} — adapted proxy loader={}",
                        modClass.getName(), adaptedBus.getClass().getClassLoader());
                return ctor.newInstance(adaptedBus);
            }

            // Name-match: (IEventBus, ModContainer)
            if (params.length == 2
                    && params[0].getName().equals("net.neoforged.bus.api.IEventBus")
                    && params[1].getName().equals("net.neoforged.fml.ModContainer")) {
                ctor.setAccessible(true);
                Object adaptedBus = adaptProxyForClassLoader(busAdapter, params[0], modClass.getClassLoader(), modBus);
                Object container = createModContainerForClassLoader(params[1], modClass.getClassLoader());
                LOGGER.info("[ReForged] Name-based fallback matched (IEventBus, ModContainer) for {}", modClass.getName());
                return ctor.newInstance(adaptedBus, container);
            }

            // Name-match: (Forge IEventBus, Forge ModContainer)
            if (params.length == 2
                    && params[0].getName().equals("net.minecraftforge.eventbus.api.IEventBus")
                    && params[1].getName().equals("net.minecraftforge.fml.ModContainer")) {
                ctor.setAccessible(true);
                LOGGER.info("[ReForged] Name-based fallback matched (Forge IEventBus, Forge ModContainer) for {}", modClass.getName());
                return ctor.newInstance(modBus, net.minecraftforge.fml.ModLoadingContext.get().getActiveContainer());
            }

            // Name-match: (Forge ModContainer, Forge IEventBus) — reversed order
            if (params.length == 2
                    && params[0].getName().equals("net.minecraftforge.fml.ModContainer")
                    && params[1].getName().equals("net.minecraftforge.eventbus.api.IEventBus")) {
                ctor.setAccessible(true);
                LOGGER.info("[ReForged] Name-based fallback matched (Forge ModContainer, Forge IEventBus) for {}", modClass.getName());
                return ctor.newInstance(net.minecraftforge.fml.ModLoadingContext.get().getActiveContainer(), modBus);
            }

            // Name-match: (ModContainer, IEventBus) — reversed order
            if (params.length == 2
                    && params[0].getName().equals("net.neoforged.fml.ModContainer")
                    && params[1].getName().equals("net.neoforged.bus.api.IEventBus")) {
                ctor.setAccessible(true);
                Object adaptedBus = adaptProxyForClassLoader(busAdapter, params[1], modClass.getClassLoader(), modBus);
                Object container = createModContainerForClassLoader(params[0], modClass.getClassLoader());
                LOGGER.info("[ReForged] Name-based fallback matched (ModContainer, IEventBus) for {}", modClass.getName());
                return ctor.newInstance(container, adaptedBus);
            }

            // Name-match: (IEventBus, Dist)
            if (params.length == 2
                    && params[0].getName().equals("net.neoforged.bus.api.IEventBus")
                    && params[1].getName().equals("net.neoforged.api.distmarker.Dist")) {
                ctor.setAccessible(true);
                Object adaptedBus = adaptProxyForClassLoader(busAdapter, params[0], modClass.getClassLoader(), modBus);
                Object dist = resolveDistForClassLoader(params[1]);
                LOGGER.info("[ReForged] Name-based fallback matched (IEventBus, Dist) for {}", modClass.getName());
                return ctor.newInstance(adaptedBus, dist);
            }

            // Name-match: (Forge IEventBus)
            if (params.length == 1 && params[0].getName().equals("net.minecraftforge.eventbus.api.IEventBus")) {
                ctor.setAccessible(true);
                LOGGER.info("[ReForged] Name-based fallback matched (Forge IEventBus) for {}", modClass.getName());
                return ctor.newInstance(modBus);
            }

            // Name-match: no-arg
            if (params.length == 0) {
                ctor.setAccessible(true);
                LOGGER.info("[ReForged] Name-based fallback matched (no-arg) for {}", modClass.getName());
                return ctor.newInstance();
            }
        }

        LOGGER.error("[ReForged] No compatible constructor found for {} (tried exact + name-based matching)", modClass.getName());
        return null;
    }

    /**
     * Re-create the busAdapter proxy so it implements the IEventBus interface from
     * the target classloader. This handles the case where the mod's IEventBus class
     * is loaded by a different classloader than the one used to create the original proxy.
     */
    private static Object adaptProxyForClassLoader(net.neoforged.bus.api.IEventBus busAdapter,
                                                    Class<?> targetIEventBus,
                                                    ClassLoader targetLoader,
                                                    IEventBus forgeModBus) {
        // If the busAdapter is already compatible, return it as-is
        if (targetIEventBus.isInstance(busAdapter)) {
            return busAdapter;
        }

        LOGGER.debug("[ReForged] Re-creating IEventBus proxy: target interface loader={}, proxy loader={}",
                targetIEventBus.getClassLoader(), busAdapter.getClass().getClassLoader());

        // Extract the original InvocationHandler from the existing proxy
        java.lang.reflect.InvocationHandler originalHandler =
                java.lang.reflect.Proxy.getInvocationHandler(busAdapter);

        // Create a new proxy that implements the target classloader's IEventBus
        return java.lang.reflect.Proxy.newProxyInstance(
                targetLoader,
                new Class<?>[]{targetIEventBus},
                (proxy, method, args) -> {
                    String name = method.getName();

                    // Delegate register() to NeoForgeEventBusAdapter's handler for full annotation scanning
                    if ("register".equals(name) && args != null && args.length == 1) {
                        NeoForgeEventBusAdapter.handleRegister(forgeModBus, args[0]);
                        return null;
                    }

                    // Forward to the original handler — it handles method delegation,
                    // toString, hashCode, equals, etc.
                    try {
                        return originalHandler.invoke(proxy, method, args);
                    } catch (java.lang.reflect.UndeclaredThrowableException e) {
                        // The original handler's Method lookups may fail across classloaders —
                        // fall back to direct method name matching on the Forge bus
                        LOGGER.debug("[ReForged] Cross-classloader invoke failed for {}, trying direct delegation", name);
                        try {
                            Method forgeMethod = findMethodByName(forgeModBus.getClass(), name, method.getParameterCount());
                            if (forgeMethod != null) {
                                forgeMethod.setAccessible(true);
                                return forgeMethod.invoke(forgeModBus, args);
                            }
                        } catch (Throwable t2) {
                            LOGGER.debug("[ReForged] Direct delegation also failed for {}: {}", name, t2.getMessage());
                        }
                    }

                    // Object methods fallback
                    if ("toString".equals(name)) return "NeoForgeEventBusAdapter[adapted-proxy]";
                    if ("hashCode".equals(name)) return System.identityHashCode(proxy);
                    if ("equals".equals(name)) return proxy == args[0];

                    return null;
                }
        );
    }

    /**
     * Find a method by name and parameter count on a class (for cross-classloader delegation).
     */
    private static Method findMethodByName(Class<?> clazz, String name, int paramCount) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                return m;
            }
        }
        return null;
    }

    /**
     * Create a ModContainer instance compatible with the target classloader.
     */
    @SuppressWarnings("removal")
    private static Object createModContainerForClassLoader(Class<?> targetModContainerClass, ClassLoader targetLoader) {
        try {
            var forgeContainer = net.minecraftforge.fml.ModLoadingContext.get().getActiveContainer();
            Constructor<?> ctor = targetModContainerClass.getDeclaredConstructor(
                    net.minecraftforge.fml.ModContainer.class);
            ctor.setAccessible(true);
            return ctor.newInstance(forgeContainer);
        } catch (Exception e) {
            LOGGER.warn("[ReForged] Failed to create cross-classloader ModContainer: {}", e.getMessage());
            return new net.neoforged.fml.ModContainer(
                    net.minecraftforge.fml.ModLoadingContext.get().getActiveContainer());
        }
    }

    /**
     * Resolve the Dist enum value from the target classloader.
     */
    @SuppressWarnings("unchecked")
    private static Object resolveDistForClassLoader(Class<?> targetDistClass) {
        try {
            String distName = net.neoforged.fml.loading.FMLEnvironment.dist.name();
            return Enum.valueOf((Class<Enum>) targetDistClass, distName);
        } catch (Exception e) {
            LOGGER.warn("[ReForged] Failed to resolve Dist for target classloader: {}", e.getMessage());
            return net.neoforged.fml.loading.FMLEnvironment.dist;
        }
    }
}
