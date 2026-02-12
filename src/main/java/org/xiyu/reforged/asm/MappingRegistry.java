package org.xiyu.reforged.asm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * MappingRegistry — Holds the remapping table for NeoForge → Forge/Shim class translations.
 *
 * <p>Mappings are loaded from {@code /reforged/mappings.json} on the classpath.
 * There are two categories:
 * <ul>
 *     <li><b>Direct mappings</b> — {@code net.neoforged.X → net.minecraftforge.X} where the APIs are
 *     signature-identical.</li>
 *     <li><b>Shim mappings</b> — {@code net.neoforged.X → org.xiyu.reforged.shim.X} where we provide
 *     a compatibility wrapper.</li>
 * </ul>
 *
 * <p>All names are stored in JVM internal format (slashes, e.g. {@code net/neoforged/neoforge/...}).
 */
public final class MappingRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MAPPINGS_RESOURCE = "/reforged/mappings.json";

    // Exact class-level overrides:  internal name → internal name
    private final Map<String, String> classMap = new HashMap<>();
    // Package-prefix redirects (longest-prefix-first at lookup time)
    private final Map<String, String> packageMap = new HashMap<>();

    private int directCount = 0;
    private int shimCount = 0;

    private static final MappingRegistry INSTANCE = new MappingRegistry();

    private MappingRegistry() {
        load();
    }

    public static MappingRegistry getInstance() {
        return INSTANCE;
    }

    // ─── Loading ───────────────────────────────────────────────────

    private void load() {
        try (InputStream is = MappingRegistry.class.getResourceAsStream(MAPPINGS_RESOURCE)) {
            if (is == null) {
                LOGGER.warn("[ReForged] mappings.json not found on classpath — no remapping rules loaded");
                return;
            }
            JsonObject root = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);

            // Package-level rules
            if (root.has("packages")) {
                JsonArray pkgs = root.getAsJsonArray("packages");
                for (JsonElement el : pkgs) {
                    JsonObject rule = el.getAsJsonObject();
                    String from = toInternal(rule.get("from").getAsString());
                    String to   = toInternal(rule.get("to").getAsString());
                    packageMap.put(from, to);
                    boolean isShim = to.startsWith("org/xiyu/reforged/shim");
                    if (isShim) shimCount++; else directCount++;
                }
            }

            // Exact class-level overrides
            if (root.has("classes")) {
                JsonArray cls = root.getAsJsonArray("classes");
                for (JsonElement el : cls) {
                    JsonObject rule = el.getAsJsonObject();
                    String from = toInternal(rule.get("from").getAsString());
                    String to   = toInternal(rule.get("to").getAsString());
                    classMap.put(from, to);
                    boolean isShim = to.startsWith("org/xiyu/reforged/shim");
                    if (isShim) shimCount++; else directCount++;
                }
            }

            LOGGER.info("[ReForged] MappingRegistry loaded {} direct mappings, {} shim mappings",
                    directCount, shimCount);
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to load mappings.json", e);
        }
    }

    // ─── Public API ────────────────────────────────────────────────

    /**
     * Remap a JVM-internal class name (e.g. {@code net/neoforged/neoforge/common/NeoForge}).
     *
     * @param internalName the original internal name
     * @return the remapped name, or the original if no mapping exists
     */
    public String remapClass(String internalName) {
        // 1. Exact class override
        String exact = classMap.get(internalName);
        if (exact != null) return exact;

        // 2. Package-prefix match (first match wins — order is insertion order)
        for (Map.Entry<String, String> entry : packageMap.entrySet()) {
            if (internalName.startsWith(entry.getKey())) {
                return entry.getValue() + internalName.substring(entry.getKey().length());
            }
        }

        return internalName;
    }

    /**
     * Test whether a given internal class name should be remapped at all.
     */
    public boolean needsRemapping(String internalName) {
        return !remapClass(internalName).equals(internalName);
    }

    public int getDirectCount() { return directCount; }
    public int getShimCount()   { return shimCount; }

    /**
     * Remap a dotted class name (e.g. {@code net.neoforged.neoforge.common.NeoForge}).
     * Convenience wrapper for code that works with dotted names (Mixin configs, ATs).
     */
    public String remapClassName(String dottedName) {
        String internal = toInternal(dottedName);
        String remapped = remapClass(internal);
        return remapped.replace('/', '.');
    }

    // ─── Helpers ───────────────────────────────────────────────────

    /** Convert dotted package/class name to JVM internal (slashes). */
    private static String toInternal(String name) {
        return name.replace('.', '/');
    }
}
