package net.neoforged.fml.config;

import java.nio.file.Path;
import net.minecraftforge.fml.ModContainer;

/**
 * Proxy: NeoForge's ModConfig.
 * Wraps Forge's config system.
 */
public class ModConfig {
    public enum Type {
        COMMON, CLIENT, SERVER, STARTUP
    }

    private final Type type;
    private final Object spec;
    private final String modId;
    private final String fileName;
    private final net.minecraftforge.fml.config.ModConfig delegate;

    public ModConfig(Type type, Object spec, ModContainer container) {
        this.type = type;
        this.spec = spec;
        this.modId = container.getModId();
        this.fileName = modId + "-" + type.name().toLowerCase() + ".toml";
        this.delegate = null;
    }

    public ModConfig(Type type, Object spec, ModContainer container, String fileName) {
        this.type = type;
        this.spec = spec;
        this.modId = container.getModId();
        this.fileName = fileName != null ? fileName : modId + "-" + type.name().toLowerCase() + ".toml";
        this.delegate = null;
    }

    /** Wrap a Forge ModConfig into our NeoForge shim. */
    public ModConfig(net.minecraftforge.fml.config.ModConfig forgeConfig) {
        this.type = Type.valueOf(forgeConfig.getType().name());
        Object forgeSpec = forgeConfig.getSpec();
        this.spec = forgeSpec instanceof net.minecraftforge.common.ForgeConfigSpec configSpec
                ? net.neoforged.neoforge.common.ModConfigSpec.wrap(configSpec)
                : forgeSpec;
        this.modId = forgeConfig.getModId();
        this.fileName = forgeConfig.getFileName();
        this.delegate = forgeConfig;
    }

    public Type getType() { return type; }
    public Object getSpec() { return spec; }
    public String getModId() { return modId; }
    public String getFileName() { return fileName; }

    public Path getFullPath() {
        return delegate != null ? delegate.getFullPath() : null;
    }

    public void acceptSyncedConfig(byte[] bytes) {
        if (delegate != null) {
            delegate.acceptSyncedConfig(bytes);
        }
    }

    public net.minecraftforge.fml.config.ModConfig unwrap() {
        return delegate;
    }
}
