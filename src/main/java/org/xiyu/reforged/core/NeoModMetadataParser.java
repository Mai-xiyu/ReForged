package org.xiyu.reforged.core;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Parses NeoForge mod metadata from {@code META-INF/neoforge.mods.toml}.
 */
public final class NeoModMetadataParser {

    private static final Logger LOGGER = LogUtils.getLogger();

    private NeoModMetadataParser() {}

    /** Mod metadata extracted from TOML. */
    public record ModMetadata(String displayName, String version,
                              String description, String license, String logoFile) {}

    /** Default metadata used when TOML parsing fails or a modId is not found. */
    public static final ModMetadata DEFAULT_METADATA =
            new ModMetadata("Unknown Mod", "1.0.0", "", "Unknown", null);

    /**
     * Parse {@code META-INF/neoforge.mods.toml} from a JAR to extract mod display metadata.
     * Falls back gracefully if TOML is missing or unparseable.
     *
     * @return map of modId → metadata; empty map on failure
     */
    public static Map<String, ModMetadata> parseModMetadata(Path jarPath) {
        Map<String, ModMetadata> result = new HashMap<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            // Resolve JAR version from manifest (for ${file.jarVersion} substitution)
            String jarVersion = "1.0.0";
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
                String implVer = manifest.getMainAttributes().getValue("Implementation-Version");
                if (implVer != null && !implVer.isEmpty()) jarVersion = implVer;
            }

            var tomlEntry = jar.getJarEntry("META-INF/neoforge.mods.toml");
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
