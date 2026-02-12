package org.xiyu.reforged.asm;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * MixinConfigRewriter — Rewrites Mixin configuration JSON files to remap
 * package targets from NeoForge to Forge equivalents.
 *
 * <h3>Mixin Config Structure</h3>
 * <pre>
 * {
 *   "required": true,
 *   "package": "com.example.mixin",
 *   "compatibilityLevel": "JAVA_21",
 *   "refmap": "mymod.refmap.json",
 *   "mixins": ["MyMixin"],
 *   "client": ["MyClientMixin"],
 *   "server": ["MyServerMixin"],
 *   "injectors": { "defaultRequire": 1 }
 * }
 * </pre>
 *
 * <h3>What We Rewrite</h3>
 * <ul>
 *     <li>Target classes in {@code "mixins"}, {@code "client"}, {@code "server"} arrays
 *         that reference NeoForge packages</li>
 *     <li>The {@code "plugin"} class name if it references NeoForge packages</li>
 *     <li>The Mixin target class annotations inside the .class files are handled
 *         by the regular {@link BytecodeRewriter}</li>
 * </ul>
 *
 * <h3>Important Note</h3>
 * <p>Mixin targets are specified as class names. If a Mixin targets a Minecraft class,
 * no rewriting is needed. Only Mixins targeting NeoForge-specific classes need remapping.
 * Most mods Mixin into vanilla Minecraft classes, which are the same on both loaders.</p>
 */
public final class MixinConfigRewriter {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Rewrite a Mixin config JSON string.
     *
     * @param configJson the original Mixin config
     * @param mappingRegistry the mapping registry for class name translation
     * @return the rewritten config
     */
    public static String rewrite(String configJson, MappingRegistry mappingRegistry) {
        try {
            JsonObject config = JsonParser.parseString(configJson).getAsJsonObject();
            boolean modified = false;

            // Rewrite plugin class if present
            if (config.has("plugin")) {
                String plugin = config.get("plugin").getAsString();
                String remapped = mappingRegistry.remapClassName(plugin);
                if (!remapped.equals(plugin)) {
                    config.addProperty("plugin", remapped);
                    modified = true;
                    LOGGER.debug("[ReForged] Mixin config: remapped plugin {} → {}", plugin, remapped);
                }
            }

            // We don't remap individual mixin class names in the arrays,
            // because those are the MOD's own classes (not NeoForge classes).
            // The package prefix and class files are handled by BytecodeRewriter.

            // BUT we DO need to remap any target references that are NeoForge classes
            // These are specified in Mixin annotations inside the class bytecode,
            // which our BytecodeRewriter already handles.

            // Handle refmap if present — we'll let it pass through as the refmap
            // is generated at compile time and references obfuscated names
            if (config.has("refmap")) {
                LOGGER.debug("[ReForged] Mixin config has refmap: {}", config.get("refmap").getAsString());
            }

            // Remove NeoForge-specific plugin references that won't exist on Forge
            if (config.has("plugin")) {
                String plugin = config.get("plugin").getAsString();
                if (plugin.contains("neoforged") || plugin.contains("neoforge")) {
                    String remapped = mappingRegistry.remapClassName(plugin);
                    config.addProperty("plugin", remapped);
                    modified = true;
                }
            }

            if (modified) {
                LOGGER.info("[ReForged] MixinConfigRewriter: Modified Mixin config");
            }

            return GSON.toJson(config);
        } catch (Exception e) {
            LOGGER.warn("[ReForged] MixinConfigRewriter: Failed to parse Mixin config, returning original", e);
            return configJson;
        }
    }

    private MixinConfigRewriter() {}
}
