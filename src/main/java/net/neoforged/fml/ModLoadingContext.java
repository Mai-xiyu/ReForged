package net.neoforged.fml;

import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;

/**
 * Proxy: NeoForge's ModLoadingContext.
 * Wraps Forge's ModLoadingContext.
 */
public class ModLoadingContext {
    private static final ModLoadingContext INSTANCE = new ModLoadingContext();

    public static ModLoadingContext get() {
        return INSTANCE;
    }

    /**
     * proxy: getActiveContainer()
     * MUST return net.neoforged.fml.ModContainer (our proxy), NOT Forge's.
     */
    @SuppressWarnings("removal")
    public ModContainer getActiveContainer() {
        // Delegate to Forge's ModLoadingContext to get the active container, then wrap it
        return ModContainer.wrap(net.minecraftforge.fml.ModLoadingContext.get().getActiveContainer());
    }

    /**
     * Returns the namespace (mod-id) of the currently active mod container.
     */
    @SuppressWarnings("removal")
    public String getActiveNamespace() {
        return net.minecraftforge.fml.ModLoadingContext.get().getActiveNamespace();
    }

    /**
     * Helper: registerConfig
     * Many mods call ModLoadingContext.get().registerConfig(...)
     */
    public void registerConfig(ModConfig.Type type, IConfigSpec spec) {
        getActiveContainer().registerConfig(type, spec);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerExtensionPoint(Class<? extends IExtensionPoint> extensionPoint,
                                        java.util.function.Supplier<? extends IExtensionPoint> extension) {
        try {
            Object ext = extension.get();
            if (ext instanceof IExtensionPoint iep) {
                getActiveContainer().registerExtensionPoint((Class) extensionPoint, iep);
            } else {
                // Handle classloader boundary: try reflection-based bridge for IConfigScreenFactory
                getActiveContainer().registerExtensionPointDynamic((Class) extensionPoint, ext);
            }
        } catch (Throwable e) {
            org.slf4j.LoggerFactory.getLogger(ModLoadingContext.class).warn(
                    "[ReForged] Failed to register extension point: {}", e.getMessage());
        }
    }

    /**
     * Direct-value overload used by NeoForge 1.21.1+ mods.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends IExtensionPoint> void registerExtensionPoint(Class<T> extensionPoint, T extension) {
        try {
            getActiveContainer().registerExtensionPoint(extensionPoint, extension);
        } catch (Throwable e) {
            org.slf4j.LoggerFactory.getLogger(ModLoadingContext.class).warn(
                    "[ReForged] Failed to register extension point: {}", e.getMessage());
        }
    }
}
