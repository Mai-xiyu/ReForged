package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import org.xiyu.reforged.asm.BytecodeRewriter;
import org.xiyu.reforged.asm.MappingRegistry;
import org.xiyu.reforged.asm.MixinConfigRewriter;
import org.xiyu.reforged.asm.ModConstructorTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/**
 * JarRewriter — Complete JAR-to-JAR transformation pipeline.
 *
 * <h3>Pipeline (Phase 3)</h3>
 * <ol>
 *     <li>{@code .class} files → {@link BytecodeRewriter} (package remap) → {@link ModConstructorTransformer}</li>
 *     <li>{@code neoforge.mods.toml} → {@link ModDescriptorConverter} → written as {@code mods.toml}</li>
 *     <li>Mixin configs ({@code *.mixins.json}) → {@link MixinConfigRewriter}</li>
 *     <li>{@code module-info.class} → stripped</li>
 *     <li>Jar-in-Jar ({@code META-INF/jarjar/*.jar}) → recursively processed</li>
 *     <li>Access Transformers ({@code accesstransformer.cfg}) → copied with remapping</li>
 *     <li>All other resources → copied as-is</li>
 * </ol>
 */
public final class JarRewriter {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final BytecodeRewriter bytecodeRewriter;
    private final MappingRegistry mappingRegistry;

    public JarRewriter() {
        this.bytecodeRewriter = new BytecodeRewriter();
        this.mappingRegistry = MappingRegistry.getInstance();
    }

    /**
     * Rewrite a NeoForge mod JAR into a Forge-compatible JAR.
     */
    public boolean rewrite(Path inputJar, Path outputJar) {
        LOGGER.info("[ReForged] JarRewriter: Processing {} → {}", inputJar.getFileName(), outputJar.getFileName());

        try {
            Files.createDirectories(outputJar.getParent());

            int classCount = 0;
            int resourceCount = 0;
            int mixinConfigCount = 0;
            int jarInJarCount = 0;
            boolean hasModsToml = false;

            try (JarFile jarFile = new JarFile(inputJar.toFile());
                 JarOutputStream jos = new JarOutputStream(
                         new BufferedOutputStream(Files.newOutputStream(outputJar)))) {

                // Track written entries to avoid duplicates
                Set<String> writtenEntries = new HashSet<>();

                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    // Skip duplicates
                    if (writtenEntries.contains(name) && !entry.isDirectory()) continue;

                    if (entry.isDirectory()) {
                        putDirEntry(jos, name, writtenEntries);
                        continue;
                    }

                    byte[] bytes = readEntry(jarFile, entry);

                    // ── Skip module-info ──
                    if (name.equals("module-info.class") || name.endsWith("/module-info.class")) {
                        LOGGER.debug("[ReForged] Stripped: {}", name);
                        continue;
                    }

                    // ── NeoForge mod descriptor → Forge mod descriptor ──
                    if (name.equals("META-INF/neoforge.mods.toml")) {
                        String neoContent = new String(bytes, StandardCharsets.UTF_8);
                        String forgeContent = ModDescriptorConverter.convert(neoContent);
                        writeEntry(jos, "META-INF/mods.toml", forgeContent.getBytes(StandardCharsets.UTF_8), writtenEntries);
                        hasModsToml = true;
                        LOGGER.info("[ReForged] Converted neoforge.mods.toml → mods.toml");
                        continue;
                    }

                    // ── Mixin config files ──
                    if (name.endsWith(".mixins.json") || name.endsWith(".mixin.json")
                            || (name.contains("mixin") && name.endsWith(".json") && !name.contains("/"))) {
                        String content = new String(bytes, StandardCharsets.UTF_8);
                        if (content.contains("\"mixins\"") || content.contains("\"package\"")) {
                            String rewritten = MixinConfigRewriter.rewrite(content, mappingRegistry);
                            writeEntry(jos, name, rewritten.getBytes(StandardCharsets.UTF_8), writtenEntries);
                            mixinConfigCount++;
                            LOGGER.info("[ReForged] Processed Mixin config: {}", name);
                            continue;
                        }
                    }

                    // ── Jar-in-Jar (nested dependencies) ──
                    if (name.startsWith("META-INF/jarjar/") && name.endsWith(".jar")) {
                        byte[] rewrittenInner = processJarInJar(bytes, name);
                        writeEntry(jos, name, rewrittenInner, writtenEntries);
                        jarInJarCount++;
                        continue;
                    }

                    // ── Access Transformers ──
                    if (name.equals("META-INF/accesstransformer.cfg") || name.endsWith("_at.cfg")) {
                        String atContent = new String(bytes, StandardCharsets.UTF_8);
                        String rewrittenAt = rewriteAccessTransformer(atContent);
                        writeEntry(jos, name, rewrittenAt.getBytes(StandardCharsets.UTF_8), writtenEntries);
                        LOGGER.info("[ReForged] Processed access transformer: {}", name);
                        continue;
                    }

                    // ── Class files: bytecode rewriting + constructor transform ──
                    if (name.endsWith(".class")) {
                        byte[] rewritten = rewriteClass(bytes);
                        writeEntry(jos, name, rewritten, writtenEntries);
                        classCount++;
                        continue;
                    }

                    // ── mods.toml (if no neoforge.mods.toml found) ──
                    if (name.equals("META-INF/mods.toml") && !hasModsToml) {
                        String content = new String(bytes, StandardCharsets.UTF_8);
                        if (content.contains("neoforge") || content.contains("NeoForge")) {
                            String converted = ModDescriptorConverter.convert(content);
                            writeEntry(jos, name, converted.getBytes(StandardCharsets.UTF_8), writtenEntries);
                            hasModsToml = true;
                            LOGGER.info("[ReForged] Converted mods.toml (NeoForge-style)");
                            continue;
                        }
                    }

                    // ── Refmap files (Mixin refmaps) ──
                    if (name.endsWith(".refmap.json")) {
                        String content = new String(bytes, StandardCharsets.UTF_8);
                        String rewritten = rewriteRefmap(content);
                        writeEntry(jos, name, rewritten.getBytes(StandardCharsets.UTF_8), writtenEntries);
                        LOGGER.debug("[ReForged] Processed refmap: {}", name);
                        continue;
                    }

                    // ── Everything else: copy as-is ──
                    writeEntry(jos, name, bytes, writtenEntries);
                    resourceCount++;
                }
            }

            LOGGER.info("[ReForged] JarRewriter: Done — {} classes, {} resources, {} Mixin configs, {} Jar-in-Jar, modsToml={}",
                    classCount, resourceCount, mixinConfigCount, jarInJarCount, hasModsToml);
            return true;

        } catch (Exception e) {
            LOGGER.error("[ReForged] JarRewriter: Failed to process {}", inputJar, e);
            return false;
        }
    }

    /**
     * Rewrite a single class: package remap → constructor transform.
     */
    private byte[] rewriteClass(byte[] original) {
        // Step 1: Package remapping
        byte[] remapped = bytecodeRewriter.rewrite(original);

        // Step 2: Constructor transform (only affects @Mod classes)
        try {
            ClassReader reader = new ClassReader(remapped);
            // Use COMPUTE_MAXS instead of COMPUTE_FRAMES to avoid frame merge errors
            // on classes whose supertype hierarchy is not available on our classpath
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ModConstructorTransformer transformer = new ModConstructorTransformer(writer);
            reader.accept(transformer, ClassReader.EXPAND_FRAMES);
            return writer.toByteArray();
        } catch (Exception e) {
            LOGGER.warn("[ReForged] Constructor transform failed, using remapped version", e);
            return remapped;
        }
    }

    /**
     * Process a nested Jar-in-Jar dependency.
     * Reads the inner JAR, rewrites its classes, and returns the rewritten JAR bytes.
     */
    private byte[] processJarInJar(byte[] jarBytes, String entryName) {
        LOGGER.info("[ReForged] Processing Jar-in-Jar: {}", entryName);
        try {
            Path tempInput = Files.createTempFile("reforged-jij-in-", ".jar");
            Path tempOutput = Files.createTempFile("reforged-jij-out-", ".jar");
            try {
                Files.write(tempInput, jarBytes);

                // Check if the inner JAR has NeoForge content
                try (JarFile inner = new JarFile(tempInput.toFile())) {
                    boolean hasNeoContent = inner.getEntry("META-INF/neoforge.mods.toml") != null
                            || inner.entries().asIterator().next() != null; // always process for safety
                }

                // Rewrite the inner JAR
                JarRewriter innerRewriter = new JarRewriter();
                if (innerRewriter.rewrite(tempInput, tempOutput)) {
                    return Files.readAllBytes(tempOutput);
                }
            } finally {
                Files.deleteIfExists(tempInput);
                Files.deleteIfExists(tempOutput);
            }
        } catch (Exception e) {
            LOGGER.warn("[ReForged] Jar-in-Jar processing failed for {}, using original", entryName, e);
        }
        return jarBytes;
    }

    /**
     * Rewrite Access Transformer entries.
     * NeoForge AT format is similar to Forge's but may reference NeoForge classes.
     */
    private String rewriteAccessTransformer(String content) {
        StringBuilder result = new StringBuilder();
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                result.append(line).append("\n");
                continue;
            }
            // AT lines: access_level class_name [field_or_method_name [descriptor]]
            // Remap class names that reference NeoForge packages
            String remapped = remapATLine(trimmed);
            result.append(remapped).append("\n");
        }
        return result.toString();
    }

    private String remapATLine(String line) {
        // Split on whitespace
        String[] parts = line.split("\\s+");
        if (parts.length >= 2) {
            // parts[1] is the class name (dotted)
            String className = parts[1];
            String remapped = mappingRegistry.remapClassName(className);
            if (!remapped.equals(className)) {
                parts[1] = remapped;
                return String.join(" ", parts);
            }
        }
        return line;
    }

    /**
     * Rewrite Mixin refmap entries.
     * Refmaps contain mappings from symbolic names to obfuscated names.
     * We need to remap any NeoForge class references.
     */
    private String rewriteRefmap(String content) {
        // Refmap is JSON — remap any class names in the values
        // Simple string-level replacement for NeoForge package prefixes
        String result = content;
        result = result.replace("net/neoforged/neoforge/", "net/minecraftforge/");
        result = result.replace("net/neoforged/fml/", "net/minecraftforge/fml/");
        result = result.replace("net/neoforged/bus/api/", "net/minecraftforge/eventbus/api/");
        result = result.replace("net.neoforged.neoforge.", "net.minecraftforge.");
        result = result.replace("net.neoforged.fml.", "net.minecraftforge.fml.");
        result = result.replace("net.neoforged.bus.api.", "net.minecraftforge.eventbus.api.");
        return result;
    }

    // ─── I/O helpers ───────────────────────────────────────────────

    private byte[] readEntry(JarFile jar, JarEntry entry) throws IOException {
        try (InputStream is = jar.getInputStream(entry)) {
            return is.readAllBytes();
        }
    }

    private void writeEntry(JarOutputStream jos, String name, byte[] data, Set<String> written) throws IOException {
        if (written.contains(name)) return;
        // Ensure parent directories exist
        ensureDirectoryEntries(jos, name, written);
        JarEntry newEntry = new JarEntry(name);
        jos.putNextEntry(newEntry);
        jos.write(data);
        jos.closeEntry();
        written.add(name);
    }

    private void putDirEntry(JarOutputStream jos, String name, Set<String> written) throws IOException {
        if (written.contains(name)) return;
        jos.putNextEntry(new JarEntry(name));
        jos.closeEntry();
        written.add(name);
    }

    private void ensureDirectoryEntries(JarOutputStream jos, String entryName, Set<String> written) throws IOException {
        String[] parts = entryName.split("/");
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            path.append(parts[i]).append("/");
            String dirName = path.toString();
            if (!written.contains(dirName)) {
                putDirEntry(jos, dirName, written);
            }
        }
    }
}
