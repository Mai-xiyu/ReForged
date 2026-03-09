package net.neoforged.fml.config;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * NeoForge-style config tracker delegating to Forge's global config tracker.
 */
public final class ConfigTracker {

    public static final ConfigTracker INSTANCE = new ConfigTracker();

    private ConfigTracker() {}

    public void loadConfigs(ModConfig.Type type, Path configBasePath) {
        net.minecraftforge.fml.config.ConfigTracker.INSTANCE.loadConfigs(toForgeType(type), configBasePath);
    }

    public void loadConfigs(ModConfig.Type type, Path configBasePath, Path serverConfigPath) {
        Path resolved = type == ModConfig.Type.SERVER && serverConfigPath != null ? serverConfigPath : configBasePath;
        loadConfigs(type, resolved);
    }

    public void unloadConfigs(ModConfig.Type type) {
        Path basePath = type == ModConfig.Type.SERVER ? FMLPaths.GAMEDIR.get().resolve("saves").resolve("serverconfig") : FMLPaths.CONFIGDIR.get();
        unloadConfigs(type, basePath);
    }

    public void unloadConfigs(ModConfig.Type type, Path configBasePath) {
        net.minecraftforge.fml.config.ConfigTracker.INSTANCE.unloadConfigs(toForgeType(type), configBasePath);
    }

    public void loadDefaultServerConfigs() {
        net.minecraftforge.fml.config.ConfigTracker.INSTANCE.loadDefaultServerConfigs();
    }

    public void acceptSyncedConfig(ModConfig config, byte[] bytes) {
        if (config != null && config.unwrap() != null) {
            config.unwrap().acceptSyncedConfig(bytes);
        }
    }

    public String getConfigFileName(String modId, ModConfig.Type type) {
        return net.minecraftforge.fml.config.ConfigTracker.INSTANCE.getConfigFileName(modId, toForgeType(type));
    }

    public Map<ModConfig.Type, Set<ModConfig>> configSets() {
        return net.minecraftforge.fml.config.ConfigTracker.INSTANCE.configSets().entrySet().stream()
                .collect(Collectors.toMap(entry -> fromForgeType(entry.getKey()), entry -> entry.getValue().stream()
                        .map(ModConfig::new)
                        .collect(Collectors.toCollection(java.util.LinkedHashSet::new)), (a, b) -> a, () -> new java.util.EnumMap<>(ModConfig.Type.class)));
    }

    public Map<String, ModConfig> fileMap() {
        return ModConfigs.getFileMap();
    }

    private static net.minecraftforge.fml.config.ModConfig.Type toForgeType(ModConfig.Type type) {
        return switch (type) {
            case COMMON, STARTUP -> net.minecraftforge.fml.config.ModConfig.Type.COMMON;
            case CLIENT -> net.minecraftforge.fml.config.ModConfig.Type.CLIENT;
            case SERVER -> net.minecraftforge.fml.config.ModConfig.Type.SERVER;
        };
    }

    private static ModConfig.Type fromForgeType(net.minecraftforge.fml.config.ModConfig.Type type) {
        return switch (type) {
            case COMMON -> ModConfig.Type.COMMON;
            case CLIENT -> ModConfig.Type.CLIENT;
            case SERVER -> ModConfig.Type.SERVER;
        };
    }
}