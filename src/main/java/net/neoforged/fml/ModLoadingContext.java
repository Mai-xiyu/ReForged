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
    public ModContainer getActiveContainer() {
        // Delegate to Forge's ModLoadingContext to get the active container, then wrap it
        return ModContainer.wrap(net.minecraftforge.fml.ModLoadingContext.get().getActiveContainer());
    }

    /**
     * Helper: registerConfig
     * Many mods call ModLoadingContext.get().registerConfig(...)
     */
    public void registerConfig(ModConfig.Type type, IConfigSpec spec) {
        getActiveContainer().registerConfig(type, spec);
    }
    
    public void registerExtensionPoint(Class<? extends IExtensionPoint> extensionPoint, java.util.function.Supplier<? extends IExtensionPoint> extension) {
         // TODO: Implement extension point registration if needed
         // For now, no-op or delegate if compatible
    }
}
