package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarFile;

/**
 * Discovers NeoForge mod JARs from the mods directory, classpath, and module path.
 *
 * <p>A NeoForge mod JAR is identified by having {@code META-INF/neoforge.mods.toml}
 * but <b>not</b> {@code META-INF/mods.toml} (which would indicate a Forge mod or
 * a Forgix-packaged dual-platform JAR).</p>
 */
public final class NeoJarDiscovery {

    private static final Logger LOGGER = LogUtils.getLogger();

    private NeoJarDiscovery() {}

    /**
     * Find JARs in modsDir that have neoforge.mods.toml but not mods.toml.
     */
    public static List<Path> discoverNeoForgeJars(Path modsDir) {
        List<Path> result = new ArrayList<>();
        try (var stream = Files.list(modsDir)) {
            for (Path path : stream.filter(p -> p.toString().endsWith(".jar")).toList()) {
                try (JarFile jar = new JarFile(path.toFile())) {
                    boolean hasNeo = jar.getJarEntry("META-INF/neoforge.mods.toml") != null;
                    boolean hasForge = jar.getJarEntry("META-INF/mods.toml") != null;
                    if (hasNeo && hasForge) {
                        LOGGER.info("[ReForged] Skipping Forgix-packaged JAR {} — has both mods.toml and neoforge.mods.toml, letting Forge handle it",
                                path.getFileName());
                    } else if (hasNeo && !hasForge) {
                        result.add(path);
                    }
                } catch (Exception e) {
                    LOGGER.debug("[ReForged] Skipping {}: {}", path.getFileName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[ReForged] Error scanning mods directory", e);
        }
        return result;
    }

    /**
     * Scan the Java classpath for NeoForge mod JARs.
     *
     * <p>In a development environment (ForgeGradle {@code runClient}), mods added as
     * {@code runtimeOnly} dependencies in {@code build.gradle} end up on the classpath
     * rather than in the {@code mods/} directory. This method discovers them by
     * checking each classpath entry for {@code META-INF/neoforge.mods.toml}.</p>
     *
     * @return list of NeoForge mod JAR paths found on the classpath
     */
    public static List<Path> discoverNeoForgeJarsOnClasspath() {
        List<Path> result = new ArrayList<>();
        try {
            String classpath = System.getProperty("java.class.path", "");
            if (classpath.isEmpty()) return result;

            String[] entries = classpath.split(System.getProperty("path.separator", ":"));
            LOGGER.debug("[ReForged] Scanning {} classpath entries for NeoForge mods", entries.length);

            for (String entry : entries) {
                if (!entry.endsWith(".jar")) continue;

                Path path = Path.of(entry);
                if (!Files.isRegularFile(path)) continue;

                // Skip Forge/Minecraft/library JARs
                String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
                if (fileName.startsWith("forge-") || fileName.startsWith("minecraft-") ||
                        fileName.startsWith("client-") || fileName.startsWith("reforged") ||
                        fileName.contains("asm-") || fileName.contains("mixin-") ||
                        fileName.contains("guava-") || fileName.contains("gson-") ||
                        fileName.contains("log4j-") || fileName.contains("slf4j-") ||
                        fileName.contains("nightconfig-") || fileName.contains("jopt-simple")) {
                    continue;
                }

                try (JarFile jar = new JarFile(path.toFile())) {
                    boolean hasNeo = jar.getJarEntry("META-INF/neoforge.mods.toml") != null;
                    boolean hasForge = jar.getJarEntry("META-INF/mods.toml") != null;
                    if (hasNeo && !hasForge) {
                        result.add(path);
                        LOGGER.debug("[ReForged] Classpath NeoForge mod: {}", path);
                    }
                } catch (Exception e) {
                    // Not a valid JAR — skip
                }
            }

            // Also check the module path if available
            String modulePath = System.getProperty("jdk.module.path", "");
            if (!modulePath.isEmpty()) {
                for (String entry : modulePath.split(System.getProperty("path.separator", ":"))) {
                    if (!entry.endsWith(".jar")) continue;
                    Path path = Path.of(entry);
                    if (!Files.isRegularFile(path)) continue;

                    try (JarFile jar = new JarFile(path.toFile())) {
                        boolean hasNeo = jar.getJarEntry("META-INF/neoforge.mods.toml") != null;
                        boolean hasForge = jar.getJarEntry("META-INF/mods.toml") != null;
                        if (hasNeo && !hasForge) {
                            result.add(path);
                            LOGGER.debug("[ReForged] Module-path NeoForge mod: {}", path);
                        }
                    } catch (Exception e) {
                        // Not a valid JAR — skip
                    }
                }
            }

            // Also scan ForgeGradle's runtime directory if exists
            // (Gradle puts runtimeOnly JARs in build/fg_cache or build/libraries)
            try {
                Path workDir = Path.of(System.getProperty("user.dir", "."));
                Path[] extraDirs = {
                        workDir.resolve("build/fg_cache"),
                        workDir.resolve("build/libraries"),
                        workDir.resolve(".gradle/loom-cache"), // in case of mixed toolchain
                };
                for (Path dir : extraDirs) {
                    if (Files.isDirectory(dir)) {
                        try (var walk = Files.walk(dir, 5)) {
                            walk.filter(p -> p.toString().endsWith(".jar"))
                                    .filter(Files::isRegularFile)
                                    .forEach(p -> {
                                        try (JarFile jar = new JarFile(p.toFile())) {
                                            boolean hasNeo = jar.getJarEntry("META-INF/neoforge.mods.toml") != null;
                                            boolean hasForge = jar.getJarEntry("META-INF/mods.toml") != null;
                                            if (hasNeo && !hasForge) {
                                                result.add(p);
                                                LOGGER.debug("[ReForged] Build-cache NeoForge mod: {}", p);
                                            }
                                        } catch (Exception e) {
                                            // Not a valid JAR — skip
                                        }
                                    });
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("[ReForged] Extra directory scan failed: {}", e.getMessage());
            }

        } catch (Exception e) {
            LOGGER.debug("[ReForged] Classpath scan failed: {}", e.getMessage());
        }
        return result;
    }
}
