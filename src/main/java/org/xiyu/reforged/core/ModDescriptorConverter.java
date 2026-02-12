package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ModDescriptorConverter — Converts NeoForge's {@code neoforge.mods.toml} to
 * Forge's {@code mods.toml} format via string manipulation.
 *
 * <h3>Key conversions:</h3>
 * <ul>
 *     <li>{@code loaderVersion="[4,)"} → {@code loaderVersion="[52,)"}</li>
 *     <li>{@code modId="neoforge"} dependency → {@code modId="forge"}</li>
 *     <li>{@code type="required"} → {@code mandatory=true}</li>
 *     <li>NeoForge version ranges → Forge version ranges</li>
 *     <li>Adds a dependency on {@code reforged} mod</li>
 * </ul>
 */
public final class ModDescriptorConverter {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Forge 1.21.1 loader/version ranges
    private static final String FORGE_LOADER_VERSION = "[52,)";
    private static final String FORGE_VERSION_RANGE = "[52,)";
    private static final String MC_VERSION_RANGE = "[1.21.1,1.22)";

    /**
     * Convert a NeoForge {@code neoforge.mods.toml} content string to
     * Forge-compatible {@code mods.toml} content.
     *
     * @param neoContent the original neoforge.mods.toml content
     * @return the converted mods.toml content
     */
    public static String convert(String neoContent) {
        String result = neoContent;

        // 1. Replace loaderVersion (NeoForge uses ~[4,), Forge uses [52,))
        result = replaceValue(result, "loaderVersion", "\"" + FORGE_LOADER_VERSION + "\"");

        // 2. Replace neoforge dependency with forge
        result = result.replace("modId=\"neoforge\"", "modId=\"forge\"");
        result = result.replace("modId = \"neoforge\"", "modId = \"forge\"");

        // 3. Replace neoforge version ranges in dependency blocks
        // NeoForge version ranges like [21.1,) → Forge ranges [52,)
        result = replaceNeoForgeVersionRange(result);

        // 4. Convert type="required" → mandatory=true  (NeoForge 1.21.1 format)
        result = result.replace("type=\"required\"", "mandatory=true");
        result = result.replace("type = \"required\"", "mandatory = true");
        result = result.replace("type=\"optional\"", "mandatory=false");
        result = result.replace("type = \"optional\"", "mandatory = false");

        // 5. Remove any NeoForge-specific keys that Forge doesn't understand
        result = removeLines(result, "enumExtensions");

        // 6. Extract all mod IDs for adding reforged dependency
        List<String> modIds = extractModIds(result);

        // 7. Append reforged dependency for each mod
        StringBuilder sb = new StringBuilder(result);
        for (String modId : modIds) {
            sb.append("\n\n# Auto-added by ReForged — ensures shim classes are available\n");
            sb.append("[[dependencies.\"").append(modId).append("\"]]\n");
            sb.append("modId=\"reforged\"\n");
            sb.append("mandatory=true\n");
            sb.append("versionRange=\"[1.0.0,)\"\n");
            sb.append("ordering=\"BEFORE\"\n");
            sb.append("side=\"BOTH\"\n");
        }
        result = sb.toString();

        LOGGER.info("[ReForged] ModDescriptorConverter: Converted neoforge.mods.toml " +
                "({} mod(s): {})", modIds.size(), modIds);

        return result;
    }

    // ─── Internal helpers ──────────────────────────────────────────

    /**
     * Replace a TOML key's value. Handles both quoted and unquoted values.
     */
    private static String replaceValue(String content, String key, String newValue) {
        // Match: key = "..." or key="..."
        Pattern p = Pattern.compile("(" + Pattern.quote(key) + "\\s*=\\s*)\"[^\"]*\"");
        Matcher m = p.matcher(content);
        if (m.find()) {
            return m.replaceAll("$1" + Matcher.quoteReplacement(newValue));
        }
        return content;
    }

    /**
     * Replace NeoForge-specific version ranges in dependency blocks.
     * Looks for versionRange values that look like NeoForge versions (e.g., [21.1,))
     * and replaces them with Forge equivalents.
     */
    private static String replaceNeoForgeVersionRange(String content) {
        // Find lines with versionRange after a neoforge→forge dependency
        // Simple approach: replace version ranges that look like NeoForge ranges
        // NeoForge uses versions like 21.x, Forge uses 52.x for 1.21.1
        String result = content;

        // Replace NeoForge version ranges (21.x pattern)
        result = result.replaceAll(
                "(versionRange\\s*=\\s*)\"\\[21\\.[^\"]*\"",
                "$1\"" + FORGE_VERSION_RANGE + "\"");

        // Also handle [20.x, [1.x (older NeoForge patterns)
        result = result.replaceAll(
                "(versionRange\\s*=\\s*)\"\\[20\\.[^\"]*\"",
                "$1\"" + FORGE_VERSION_RANGE + "\"");

        return result;
    }

    /**
     * Remove entire lines containing a specific key.
     */
    private static String removeLines(String content, String key) {
        return content.replaceAll("(?m)^.*" + Pattern.quote(key) + ".*$\\n?", "");
    }

    /**
     * Extract all modId values from [[mods]] sections.
     */
    private static List<String> extractModIds(String content) {
        List<String> ids = new ArrayList<>();
        Pattern p = Pattern.compile("modId\\s*=\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(content);
        boolean inModsSection = false;
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.equals("[[mods]]")) {
                inModsSection = true;
                continue;
            }
            if (trimmed.startsWith("[[") && !trimmed.equals("[[mods]]")) {
                inModsSection = false;
            }
            if (inModsSection) {
                Matcher lm = p.matcher(trimmed);
                if (lm.find()) {
                    ids.add(lm.group(1));
                    inModsSection = false;
                }
            }
        }
        if (ids.isEmpty()) {
            // Fallback: grab first modId anywhere
            Matcher fm = p.matcher(content);
            if (fm.find()) ids.add(fm.group(1));
        }
        return ids;
    }
}
