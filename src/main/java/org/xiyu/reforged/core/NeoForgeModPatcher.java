package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.jar.*;
import java.util.zip.*;

/**
 * NeoForgeModPatcher — Scans a mods directory and patches NeoForge JARs in-place.
 *
 * <h3>What it does:</h3>
 * <p>For each JAR in the directory that contains {@code META-INF/neoforge.mods.toml}
 * but lacks {@code META-INF/mods.toml}, it injects a converted {@code mods.toml}
 * so Forge's mod discovery will recognize and load the mod.</p>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * // From Gradle:
 * java -cp ... org.xiyu.reforged.core.NeoForgeModPatcher <modsDir>
 * }</pre>
 *
 * <p>This is designed to run BEFORE game launch. It modifies JARs in-place
 * (backup is created with .bak extension).</p>
 */
public final class NeoForgeModPatcher {

    private static final Logger LOGGER;

    static {
        Logger tempLogger;
        try {
            tempLogger = LogUtils.getLogger();
        } catch (Throwable t) {
            tempLogger = null;
        }
        LOGGER = tempLogger;
    }

    private static void log(String msg) {
        if (LOGGER != null) LOGGER.info(msg);
        else System.out.println(msg);
    }

    private static void warn(String msg, Throwable t) {
        if (LOGGER != null) LOGGER.warn(msg, t);
        else { System.err.println(msg); if (t != null) t.printStackTrace(); }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: NeoForgeModPatcher <modsDir>");
            System.exit(1);
        }
        Path modsDir = Path.of(args[0]);
        int patched = patchAll(modsDir);
        log("[ReForged] NeoForgeModPatcher: " + patched + " NeoForge mod(s) patched in " + modsDir);
    }

    /**
     * Patch all NeoForge JARs in the given directory.
     * @return number of JARs patched
     */
    public static int patchAll(Path modsDir) {
        if (!Files.isDirectory(modsDir)) return 0;

        int count = 0;
        try (var stream = Files.list(modsDir)) {
            for (Path jar : stream.filter(p -> p.toString().endsWith(".jar")).toList()) {
                if (patchIfNeeded(jar)) count++;
            }
        } catch (Exception e) {
            warn("[ReForged] Error scanning mods directory: " + modsDir, e);
        }
        return count;
    }

    /**
     * Check if a JAR is a NeoForge mod and patch it if needed.
     * @return true if the JAR was patched
     */
    public static boolean patchIfNeeded(Path jarPath) {
        String neoContent;
        byte[][] entryData;
        String[] entryNames;
        int entryCount;

        // Phase 1: Read everything, then CLOSE the JAR to release Windows file locks
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            // Already has mods.toml — skip
            if (jar.getEntry("META-INF/mods.toml") != null) return false;

            // Check for neoforge.mods.toml
            JarEntry neoEntry = jar.getJarEntry("META-INF/neoforge.mods.toml");
            if (neoEntry == null) return false;

            // Read the NeoForge descriptor
            try (InputStream is = jar.getInputStream(neoEntry)) {
                neoContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            // Read ALL entries into memory
            var entries = jar.entries();
            var nameList = new java.util.ArrayList<String>();
            var dataList = new java.util.ArrayList<byte[]>();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                nameList.add(entry.getName());
                if (entry.isDirectory()) {
                    dataList.add(new byte[0]);
                } else {
                    try (InputStream is = jar.getInputStream(entry)) {
                        dataList.add(is.readAllBytes());
                    }
                }
            }
            entryCount = nameList.size();
            entryNames = nameList.toArray(new String[0]);
            entryData = dataList.toArray(new byte[0][]);
        } catch (Exception e) {
            warn("[ReForged] Failed to read JAR: " + jarPath.getFileName(), e);
            return false;
        }
        // JAR is now CLOSED — file lock released on Windows

        // Phase 2: Convert descriptor
        String forgeContent = ModDescriptorConverter.convert(neoContent);
        log("[ReForged] Patching NeoForge mod: " + jarPath.getFileName());

        // Phase 3: Write new JAR from memory
        Path tempJar = jarPath.resolveSibling(jarPath.getFileName() + ".tmp");
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(tempJar.toFile()))) {
            for (int i = 0; i < entryCount; i++) {
                out.putNextEntry(new JarEntry(entryNames[i]));
                if (entryData[i].length > 0) {
                    out.write(entryData[i]);
                }
                out.closeEntry();
            }
            // Add mods.toml
            out.putNextEntry(new JarEntry("META-INF/mods.toml"));
            out.write(forgeContent.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        } catch (Exception e) {
            warn("[ReForged] Failed to write patched JAR: " + jarPath.getFileName(), e);
            return false;
        }

        // Phase 4: Replace original (no locks held)
        try {
            Path backup = jarPath.resolveSibling(jarPath.getFileName() + ".neoforge-original");
            if (!Files.exists(backup)) {
                Files.copy(jarPath, backup);
            }
            Files.move(tempJar, jarPath, StandardCopyOption.REPLACE_EXISTING);
            log("[ReForged] Patched: " + jarPath.getFileName() + " (original backed up as .neoforge-original)");
            return true;
        } catch (Exception e) {
            warn("[ReForged] Failed to replace JAR: " + jarPath.getFileName(), e);
            return false;
        }
    }
}
