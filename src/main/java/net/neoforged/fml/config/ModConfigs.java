package net.neoforged.fml.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * NeoForge-style view of registered mod configs backed by Forge's {@code ConfigTracker}.
 */
public final class ModConfigs {

    private ModConfigs() {}

    public static Map<String, ModConfig> getFileMap() {
        return net.minecraftforge.fml.config.ConfigTracker.INSTANCE.fileMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ModConfig(entry.getValue())));
    }

    public static Set<ModConfig> getConfigSet(ModConfig.Type type) {
        var forgeType = toForgeType(type);
        return net.minecraftforge.fml.config.ConfigTracker.INSTANCE.configSets()
                .getOrDefault(forgeType, Set.of())
                .stream()
                .map(ModConfig::new)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    public static List<String> getConfigFileNames(String modId, ModConfig.Type type) {
        return getConfigSet(type).stream()
                .filter(config -> config.getModId().equals(modId))
                .map(ModConfig::getFileName)
                .toList();
    }

    private static net.minecraftforge.fml.config.ModConfig.Type toForgeType(ModConfig.Type type) {
        return switch (type) {
            case COMMON, STARTUP -> net.minecraftforge.fml.config.ModConfig.Type.COMMON;
            case CLIENT -> net.minecraftforge.fml.config.ModConfig.Type.CLIENT;
            case SERVER -> net.minecraftforge.fml.config.ModConfig.Type.SERVER;
        };
    }
}