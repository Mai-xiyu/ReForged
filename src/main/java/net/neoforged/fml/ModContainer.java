package net.neoforged.fml;

/**
 * Proxy for NeoForge's {@code ModContainer}.
 * NeoForge mods may receive a ModContainer in their constructor.
 *
 * <p>Since Forge's ModContainer is abstract with complex constructor requirements,
 * we use composition. NeoForge mods that reference {@code net.neoforged.fml.ModContainer}
 * will find this class, and the Mixin in FMLModContainer handles passing the
 * correct container to the mod constructor.</p>
 */
public class ModContainer {

    private static net.neoforged.bus.api.IEventBus globalModBus;

    private final net.minecraftforge.fml.ModContainer delegate;
    private net.neoforged.bus.api.IEventBus eventBus;

    public ModContainer(net.minecraftforge.fml.ModContainer delegate) {
        this.delegate = delegate;
        this.eventBus = globalModBus;
    }

    /**
     * Set the global mod event bus used by all NeoForge ModContainer wrappers.
     * Called once during NeoForgeModLoader initialization.
     */
    public static void setGlobalModBus(net.neoforged.bus.api.IEventBus bus) {
        globalModBus = bus;
    }

    /**
     * Get the mod event bus for this container.
     * NeoForge mods (e.g. TwilightForest BeanContext) call this to register listeners.
     */
    public net.neoforged.bus.api.IEventBus getEventBus() {
        return eventBus;
    }

    /**
     * Get the mod ID.
     */
    public String getModId() {
        return delegate.getModId();
    }

    /**
     * Get the namespace (same as mod ID in most cases).
     */
    public String getNamespace() {
        return delegate.getModId();
    }

    /**
     * Get the mod info.
     * Returns Forge's IModInfo directly since after bytecode remapping,
     * NeoForge callers expect {@code net.minecraftforge.forgespi.language.IModInfo}
     * as the return type (neoforgespi → forgespi remapping).
     */
    public net.minecraftforge.forgespi.language.IModInfo getModInfo() {
        return delegate.getModInfo();
    }

    /**
     * Get the underlying Forge container.
     */
    public net.minecraftforge.fml.ModContainer getForgeContainer() {
        return delegate;
    }

    /**
     * Wrap a Forge ModContainer for NeoForge mod constructors.
     */
    public static ModContainer wrap(net.minecraftforge.fml.ModContainer forgeContainer) {
        return new ModContainer(forgeContainer);
    }

    public static ModContainer wrap(net.minecraftforge.fml.ModContainer forgeContainer, net.neoforged.bus.api.IEventBus bus) {
        ModContainer mc = new ModContainer(forgeContainer);
        mc.eventBus = bus;
        return mc;
    }

    /**
     * Register a config spec with this mod container.
     * NeoForge mods call this to associate configs with their mod.
     */
    public void registerConfig(net.neoforged.fml.config.ModConfig.Type type,
                               net.neoforged.fml.config.IConfigSpec spec) {
        net.minecraftforge.fml.config.ModConfig.Type forgeType = toForgeType(type);
        net.minecraftforge.common.ForgeConfigSpec forgeSpec = toForgeConfigSpec(spec);
        if (forgeSpec != null) {
            delegate.addConfig(new net.minecraftforge.fml.config.ModConfig(forgeType, forgeSpec, delegate));
        }
    }

    /**
     * Overload accepting Forge types directly — bytecode-remapped NeoForge mods
     * may call registerConfig with Forge-typed parameters after class remapping.
     */
    public void registerConfig(net.minecraftforge.fml.config.ModConfig.Type forgeType,
                               net.minecraftforge.fml.config.IConfigSpec<?> spec) {
        net.minecraftforge.common.ForgeConfigSpec forgeSpec = toForgeConfigSpecFromForge(spec);
        if (forgeSpec != null) {
            delegate.addConfig(new net.minecraftforge.fml.config.ModConfig(forgeType, forgeSpec, delegate));
        }
    }

    /**
     * Overload with filename, accepting Forge types directly.
     */
    public void registerConfig(net.minecraftforge.fml.config.ModConfig.Type forgeType,
                               net.minecraftforge.fml.config.IConfigSpec<?> spec, String fileName) {
        net.minecraftforge.common.ForgeConfigSpec forgeSpec = toForgeConfigSpecFromForge(spec);
        if (forgeSpec != null) {
            delegate.addConfig(new net.minecraftforge.fml.config.ModConfig(forgeType, forgeSpec, delegate, fileName));
        }
    }

    /**
     * Register an extension point with this mod container (NeoForge typed).
     * Bridges NeoForge's IConfigScreenFactory to Forge's ConfigScreenHandler.
     */
    @SuppressWarnings("unchecked")
    public <T extends IExtensionPoint> void registerExtensionPoint(Class<T> point, T extension) {
        registerExtensionPointDynamic(point, extension);
    }

    /**
     * Register an extension point with a Supplier (NeoForge typed).
     * Some NeoForge mods use the Supplier variant.
     */
    @SuppressWarnings("unchecked")
    public <T extends IExtensionPoint> void registerExtensionPoint(Class<T> point, java.util.function.Supplier<T> supplier) {
        registerExtensionPointDynamic(point, supplier.get());
    }

    /**
     * Register an extension point with Forge-typed parameter.
     * After bytecode remapping, NeoForge IExtensionPoint becomes Forge IExtensionPoint.
     */
    @SuppressWarnings("unchecked")
    public <T extends net.minecraftforge.fml.IExtensionPoint> void registerExtensionPoint(Class<T> point, T extension) {
        registerExtensionPointDynamic(point, extension);
    }

    // Note: Forge-typed Supplier overload omitted — same erasure as NeoForge-typed Supplier overload.
    // The NeoForge-typed version handles both cases since callers use net.neoforged.fml.IExtensionPoint.

    /**
     * Dynamic extension point registration for cross-classloader objects.
     * Uses reflection to detect IConfigScreenFactory when direct instanceof fails.
     */
    void registerExtensionPointDynamic(Class<?> point, Object extension) {
        if (extension instanceof net.neoforged.neoforge.client.gui.IConfigScreenFactory) {
            bridgeConfigScreenFactory(extension);
            return;
        }
        // Try reflection: the object might implement IConfigScreenFactory from a different classloader
        try {
            java.lang.reflect.Method createScreen = null;
            for (var iface : extension.getClass().getInterfaces()) {
                if (iface.getName().equals("net.neoforged.neoforge.client.gui.IConfigScreenFactory")) {
                    createScreen = iface.getMethod("createScreen",
                            Class.forName("net.neoforged.fml.ModContainer", false, iface.getClassLoader()),
                            net.minecraft.client.gui.screens.Screen.class);
                    break;
                }
            }
            if (createScreen != null) {
                final java.lang.reflect.Method m = createScreen;
                java.util.function.BiFunction<net.minecraft.client.Minecraft, net.minecraft.client.gui.screens.Screen,
                        net.minecraft.client.gui.screens.Screen> forgeFactory =
                        (mc, parent) -> {
                            try {
                                return (net.minecraft.client.gui.screens.Screen) m.invoke(extension, ModContainer.this, parent);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        };
                var configScreen = new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory(forgeFactory);
                delegate.registerExtensionPoint(
                        net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> configScreen);
                org.slf4j.LoggerFactory.getLogger(ModContainer.class).info(
                        "[ReForged] Bridged config screen factory for mod '{}' via reflection", getModId());
                return;
            }
        } catch (Throwable e) {
            org.slf4j.LoggerFactory.getLogger(ModContainer.class).debug(
                    "[ReForged] Reflection bridge failed for mod '{}': {}", getModId(), e.getMessage());
        }
        org.slf4j.LoggerFactory.getLogger(ModContainer.class).debug(
                "[ReForged] Ignoring unknown extension point for mod '{}': {}", getModId(), extension.getClass().getName());
    }

    private void bridgeConfigScreenFactory(Object extension) {
        if (extension instanceof net.neoforged.neoforge.client.gui.IConfigScreenFactory factory) {
            try {
                java.util.function.BiFunction<net.minecraft.client.Minecraft, net.minecraft.client.gui.screens.Screen,
                        net.minecraft.client.gui.screens.Screen> forgeFactory =
                        (mc, parent) -> factory.createScreen(ModContainer.this, parent);
                var configScreen = new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory(forgeFactory);
                delegate.registerExtensionPoint(
                        net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> configScreen);
            } catch (Throwable e) {
                org.slf4j.LoggerFactory.getLogger(ModContainer.class).warn(
                        "[ReForged] Failed to bridge config screen factory for mod '{}': {}",
                        getModId(), e.getMessage());
            }
        }
    }

    /**
     * Get a custom extension point registered on this container.
     */
    @SuppressWarnings("unchecked")
    public <T extends IExtensionPoint> java.util.Optional<T> getCustomExtension(Class<T> point) {
        if (point == net.neoforged.neoforge.client.gui.IConfigScreenFactory.class) {
            try {
                var opt = delegate.getCustomExtension(
                        net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class);
                if (opt.isPresent()) {
                    var forgeFactory = opt.get();
                    net.neoforged.neoforge.client.gui.IConfigScreenFactory wrapper =
                            (container, parent) -> forgeFactory.screenFunction().apply(
                                    net.minecraft.client.Minecraft.getInstance(), parent);
                    return java.util.Optional.of((T) wrapper);
                }
            } catch (Throwable ignored) {}
        }
        return java.util.Optional.empty();
    }

    /**
     * Register a config spec with a custom filename.
     */
    public void registerConfig(net.neoforged.fml.config.ModConfig.Type type,
                               net.neoforged.fml.config.IConfigSpec spec, String fileName) {
        net.minecraftforge.fml.config.ModConfig.Type forgeType = toForgeType(type);
        net.minecraftforge.common.ForgeConfigSpec forgeSpec = toForgeConfigSpec(spec);
        if (forgeSpec != null) {
            delegate.addConfig(new net.minecraftforge.fml.config.ModConfig(forgeType, forgeSpec, delegate, fileName));
        }
    }

    private static net.minecraftforge.fml.config.ModConfig.Type toForgeType(net.neoforged.fml.config.ModConfig.Type type) {
        return switch (type) {
            case COMMON -> net.minecraftforge.fml.config.ModConfig.Type.COMMON;
            case CLIENT -> net.minecraftforge.fml.config.ModConfig.Type.CLIENT;
            case SERVER -> net.minecraftforge.fml.config.ModConfig.Type.SERVER;
            case STARTUP -> net.minecraftforge.fml.config.ModConfig.Type.COMMON;
        };
    }

    private net.minecraftforge.common.ForgeConfigSpec toForgeConfigSpec(net.neoforged.fml.config.IConfigSpec spec) {
        if (spec == null) {
            return null;
        }
        if (spec instanceof net.neoforged.neoforge.common.ModConfigSpec mcs) {
            return mcs.getForgeSpec();
        }
        if (spec instanceof net.minecraftforge.common.ForgeConfigSpec fcs) {
            return fcs;
        }

        try {
            java.lang.reflect.Method method = spec.getClass().getMethod("getForgeSpec");
            Object value = method.invoke(spec);
            if (value instanceof net.minecraftforge.common.ForgeConfigSpec fcs) {
                return fcs;
            }
        } catch (Throwable ignored) {
        }

        try {
            java.lang.reflect.Method method = spec.getClass().getMethod("getSpec");
            Object value = method.invoke(spec);
            if (value instanceof net.minecraftforge.common.ForgeConfigSpec fcs) {
                return fcs;
            }
        } catch (Throwable ignored) {
        }

        try {
            java.lang.reflect.Field field = spec.getClass().getDeclaredField("forgeSpec");
            field.setAccessible(true);
            Object value = field.get(spec);
            if (value instanceof net.minecraftforge.common.ForgeConfigSpec fcs) {
                return fcs;
            }
        } catch (Throwable ignored) {
        }

        org.slf4j.LoggerFactory.getLogger(ModContainer.class).warn(
                "[ReForged] Unsupported NeoForge IConfigSpec implementation for mod '{}': {}",
                getModId(), spec.getClass().getName());
        return null;
    }

    /**
     * Extract ForgeConfigSpec from Forge's own IConfigSpec (used by Forge-typed overloads).
     */
    private net.minecraftforge.common.ForgeConfigSpec toForgeConfigSpecFromForge(net.minecraftforge.fml.config.IConfigSpec<?> spec) {
        if (spec == null) {
            return null;
        }
        if (spec instanceof net.minecraftforge.common.ForgeConfigSpec fcs) {
            return fcs;
        }
        // ModConfigSpec now implements Forge's IConfigSpec
        if (spec instanceof net.neoforged.neoforge.common.ModConfigSpec mcs) {
            return mcs.getForgeSpec();
        }
        // Reflection fallback
        try {
            java.lang.reflect.Method method = spec.getClass().getMethod("getForgeSpec");
            Object value = method.invoke(spec);
            if (value instanceof net.minecraftforge.common.ForgeConfigSpec fcs) {
                return fcs;
            }
        } catch (Throwable ignored) {}
        org.slf4j.LoggerFactory.getLogger(ModContainer.class).warn(
                "[ReForged] Unsupported Forge IConfigSpec implementation for mod '{}': {}",
                getModId(), spec.getClass().getName());
        return null;
    }
}
