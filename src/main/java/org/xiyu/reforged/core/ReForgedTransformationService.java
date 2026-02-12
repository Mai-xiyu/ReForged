package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

/**
 * ReForgedTransformationService — The earliest entry point for ReForged.
 *
 * <p>In Forge 1.21.1 the mod loading pipeline is built on top of
 * <b>cpw.mods.modlauncher</b>. An {@code ITransformationService} registered via
 * {@code META-INF/services/} is loaded before any mods and has the opportunity to:
 * <ol>
 *     <li>Add additional class-transforming hooks</li>
 *     <li>Register custom resource locators</li>
 *     <li>Modify the classpath</li>
 * </ol>
 *
 * <p><b>Skeleton implementation:</b> In this phase we simply initialize our subsystems
 * and log startup. Full transformer registration (connecting {@code BytecodeRewriter} as
 * an {@code ITransformer}) will be added once we need to rewrite classes that are loaded
 * through the normal classpath rather than through our {@code NeoForgeModLocator}.
 *
 * <h3>Why not implement ITransformationService directly?</h3>
 * <p>The ModLauncher {@code ITransformationService} interface lives in the
 * {@code cpw.mods.modlauncher} module, which is <em>not</em> a compile-time dependency
 * in the standard Forge MDK. To keep the skeleton compilable without pulling in
 * ModLauncher as a dependency, we implement this as a plain class with the right
 * method signatures. At runtime on a real Forge client, we would provide a proper
 * thin adapter. For the skeleton we document the intended contract.</p>
 */
public final class ReForgedTransformationService {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String NAME = "reforged";

    // ─── ITransformationService contract (method signatures) ──────

    public String name() {
        return NAME;
    }

    /**
     * Called during the {@code initialize} phase of ModLauncher.
     * We use this to scan for NeoForge mod JARs and prepare mapping tables.
     */
    public void initialize(Object environment) {
        LOGGER.info("[ReForged] TransformationService initializing...");
        // The MappingRegistry singleton auto-loads mappings.json on first access.
        // We trigger it here to fail-fast if the config is broken.
        org.xiyu.reforged.asm.MappingRegistry registry =
                org.xiyu.reforged.asm.MappingRegistry.getInstance();
        LOGGER.info("[ReForged] Loaded {} direct + {} shim mappings",
                registry.getDirectCount(), registry.getShimCount());
    }

    /**
     * Called to query which transformers this service provides.
     * Skeleton: returns an empty list — we perform rewriting at JAR-load time
     * through {@link NeoForgeModLocator} instead.
     */
    public List<?> transformers() {
        return List.of();
    }

    // ─── Static helper ─────────────────────────────────────────────

    /**
     * Scan a directory for JARs that look like NeoForge mods
     * (i.e., their {@code mods.toml} references {@code neoforge} loader).
     */
    public static boolean isNeoForgeModJar(Path jarPath) {
        if (!jarPath.toString().endsWith(".jar")) return false;
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            // NeoForge mods use "lowcodefml" or "javafml" with a dependency on neoforge
            Manifest manifest = jar.getManifest();
            if (jar.getEntry("META-INF/neoforge.mods.toml") != null) {
                return true;
            }
            // Some NeoForge mods still use mods.toml but depend on neoforge
            if (jar.getEntry("META-INF/mods.toml") != null) {
                var entry = jar.getEntry("META-INF/mods.toml");
                try (var is = jar.getInputStream(entry)) {
                    String content = new String(is.readAllBytes());
                    return content.contains("neoforge") || content.contains("NeoForge");
                }
            }
        } catch (IOException e) {
            LOGGER.debug("[ReForged] Could not inspect JAR: {}", jarPath, e);
        }
        return false;
    }

    /**
     * Scan the {@code neoforge-mods/} directory relative to the game directory.
     */
    public static List<Path> discoverNeoForgeJars(Path gameDir) {
        Path neoModsDir = gameDir.resolve("neoforge-mods");
        if (!Files.isDirectory(neoModsDir)) {
            LOGGER.info("[ReForged] No neoforge-mods/ directory found at {}", neoModsDir);
            return List.of();
        }
        try (Stream<Path> files = Files.list(neoModsDir)) {
            List<Path> jars = files.filter(ReForgedTransformationService::isNeoForgeModJar).toList();
            LOGGER.info("[ReForged] Discovered {} NeoForge mod JAR(s) in {}", jars.size(), neoModsDir);
            return jars;
        } catch (IOException e) {
            LOGGER.error("[ReForged] Failed to scan neoforge-mods/", e);
            return List.of();
        }
    }
}
