package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import org.xiyu.reforged.asm.BytecodeRewriter;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * NeoForgeModLocator — Discovers NeoForge mod JARs from the {@code neoforge-mods/}
 * directory and presents them to Forge's mod loading pipeline after bytecode rewriting.
 *
 * <h3>How it works</h3>
 * <ol>
 *     <li>Forge's FML calls {@link #scanMods()} during mod discovery.</li>
 *     <li>We list all JARs in {@code <gamedir>/neoforge-mods/}.</li>
 *     <li>For each JAR, we read every {@code .class} entry, pass it through
 *         {@link BytecodeRewriter}, and cache the rewritten bytes.</li>
 *     <li>The rewritten mod is then presented to FML as a virtual mod source.</li>
 * </ol>
 *
 * <p><b>Note:</b> The actual {@code IModLocator} interface from
 * {@code net.minecraftforge.forgespi.locating} is not available as a compile-time
 * dependency in the standard MDK. This class documents the contract and provides
 * the implementation logic. At runtime on a real Forge client it would be
 * registered via the SPI file.</p>
 *
 * <p>In the skeleton phase this class can be invoked directly from the mod's
 * constructor for demonstration/testing purposes.</p>
 */
public final class NeoForgeModLocator {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final BytecodeRewriter rewriter;
    private final Path neoModsDir;

    /**
     * Each discovered NeoForge mod is represented as a map of
     * internal-class-name → rewritten bytes.
     */
    public record RewrittenMod(
            Path originalJar,
            String modId,
            Map<String, byte[]> rewrittenClasses,
            Map<String, byte[]> resources
    ) {}

    public NeoForgeModLocator(Path gameDir) {
        this.rewriter = new BytecodeRewriter();
        this.neoModsDir = gameDir.resolve("neoforge-mods");
    }

    // ─── IModLocator contract ──────────────────────────────────────

    public String name() {
        return "reforged-neo-locator";
    }

    /**
     * Scan for NeoForge mods and rewrite their bytecode.
     *
     * @return a list of rewritten mods ready for injection into Forge's pipeline
     */
    public List<RewrittenMod> scanMods() {
        List<Path> jars = ReForgedTransformationService.discoverNeoForgeJars(neoModsDir.getParent());
        if (jars.isEmpty()) return List.of();

        List<RewrittenMod> results = new ArrayList<>();
        for (Path jar : jars) {
            try {
                RewrittenMod mod = processJar(jar);
                if (mod != null) {
                    results.add(mod);
                    LOGGER.info("[ReForged] Successfully rewritten NeoForge mod: {} ({} classes, {} resources)",
                            mod.modId(), mod.rewrittenClasses().size(), mod.resources().size());
                }
            } catch (Exception e) {
                LOGGER.error("[ReForged] Failed to process NeoForge mod JAR: {}", jar, e);
            }
        }
        return results;
    }

    // ─── JAR processing ────────────────────────────────────────────

    private RewrittenMod processJar(Path jarPath) throws IOException {
        Map<String, byte[]> rewrittenClasses = new LinkedHashMap<>();
        Map<String, byte[]> resources = new LinkedHashMap<>();
        String modId = "unknown";

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;

                String entryName = entry.getName();
                byte[] bytes = readEntry(jarFile, entry);

                if (entryName.endsWith(".class")) {
                    // Rewrite the class bytecode
                    String className = entryName.substring(0, entryName.length() - 6); // strip .class
                    byte[] rewritten = rewriter.rewrite(bytes);
                    rewrittenClasses.put(className, rewritten);
                } else {
                    // Preserve resources as-is
                    resources.put(entryName, bytes);

                    // Try to extract mod ID from mods.toml / neoforge.mods.toml
                    if (entryName.equals("META-INF/neoforge.mods.toml") || entryName.equals("META-INF/mods.toml")) {
                        modId = extractModId(new String(bytes));
                    }
                }
            }
        }

        if (rewrittenClasses.isEmpty()) {
            LOGGER.warn("[ReForged] JAR {} contained no .class files, skipping", jarPath);
            return null;
        }

        return new RewrittenMod(jarPath, modId, rewrittenClasses, resources);
    }

    private byte[] readEntry(JarFile jar, JarEntry entry) throws IOException {
        try (InputStream is = jar.getInputStream(entry)) {
            return is.readAllBytes();
        }
    }

    /**
     * Naive extraction of modId from a TOML file.
     * Looks for {@code modId = "..."} line.
     */
    private String extractModId(String tomlContent) {
        for (String line : tomlContent.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("modId")) {
                int eqIdx = trimmed.indexOf('=');
                if (eqIdx > 0) {
                    String value = trimmed.substring(eqIdx + 1).trim();
                    // Strip quotes
                    value = value.replace("\"", "").replace("'", "").trim();
                    if (!value.isEmpty()) return value;
                }
            }
        }
        return "unknown";
    }
}
