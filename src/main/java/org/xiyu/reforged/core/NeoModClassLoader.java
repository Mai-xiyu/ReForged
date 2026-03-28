package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Creates a child-first (parent-last) {@link URLClassLoader} for NeoForge mod JARs.
 *
 * <p>Also extracts nested Jar-in-Jar (JiJ) dependencies from
 * {@code META-INF/jarjar/} inside each mod JAR and adds them
 * to the classloader so library classes are available.</p>
 */
public final class NeoModClassLoader {

    private static final Logger LOGGER = LogUtils.getLogger();

    private NeoModClassLoader() {}

    /**
     * Create a URLClassLoader for the NeoForge mod JARs.
     * Parent is the given classloader (game + Forge + ReForged).
     *
     * @param jars        list of NeoForge mod JAR paths
     * @param parentLoader the parent classloader (typically the game classloader)
     * @return the classloader, or null on failure
     */
    public static URLClassLoader createClassLoader(List<Path> jars, ClassLoader parentLoader) {
        try {
            List<URL> urls = new ArrayList<>();
            // Add top-level mod JARs
            for (Path jar : jars) {
                urls.add(jar.toUri().toURL());
            }
            // Extract Jar-in-Jar (JiJ) dependencies
            Path jijTemp = Files.createTempDirectory("reforged-jij-");
            jijTemp.toFile().deleteOnExit();
            for (Path jar : jars) {
                try (JarFile jarFile = new JarFile(jar.toFile())) {
                    var entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith("META-INF/jarjar/") && name.endsWith(".jar") && !entry.isDirectory()) {
                            // Extract nested JAR to temp directory
                            String fileName = name.substring(name.lastIndexOf('/') + 1);
                            Path extracted = jijTemp.resolve(fileName);
                            try (InputStream is = jarFile.getInputStream(entry)) {
                                Files.copy(is, extracted, StandardCopyOption.REPLACE_EXISTING);
                            }
                            extracted.toFile().deleteOnExit();
                            urls.add(extracted.toUri().toURL());
                            LOGGER.info("[ReForged] Extracted JiJ dependency: {} from {}", fileName, jar.getFileName());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("[ReForged] Failed to extract JiJ from {}: {}", jar.getFileName(), e.getMessage());
                }
            }
            // Use a CHILD-FIRST (parent-last) classloader so that mod classes
            // are loaded from our URLs rather than from AppClassLoader (which sees
            // them on the system classpath via runtimeOnly dependencies).
            // This ensures mod classes resolve IEventBus/etc. through our parent
            // (TransformingClassLoader) rather than AppClassLoader, avoiding the
            // classloader identity mismatch that breaks constructor injection.
            return new URLClassLoader(urls.toArray(new URL[0]), parentLoader) {

                // Packages that must always be loaded from the parent classloader
                private static final String[] PARENT_FIRST_PREFIXES = {
                    "java.", "jdk.", "sun.", "javax.",               // JDK
                    "net.minecraft.", "com.mojang.",                 // Minecraft
                    "net.minecraftforge.", "net.neoforged.",         // Forge & NeoForge shims
                    "org.xiyu.reforged.",                            // ReForged
                    "net.blay09.mods.balm.api.entity.",             // BalmEntity API only (shared type identity)
                    "cpw.mods.",                                     // ModLauncher
                    "org.objectweb.asm.",                            // ASM
                    "org.slf4j.", "org.apache.logging.",             // Logging
                    "com.google.", "org.apache.commons.",            // Common libs
                    "io.netty.", "it.unimi.dsi.",                    // Netty & fastutil
                    "org.spongepowered.",                            // Mixin
                };

                // Specific classes that must be loaded from parent to maintain type identity
                // with classes injected via ReForged Mixins (e.g., JadeFont interface on Font)
                private static final java.util.Set<String> PARENT_FIRST_CLASSES = java.util.Set.of(
                    "snownee.jade.gui.JadeFont",
                    "snownee.jade.mixin.EntityAccess"
                );

                private boolean isParentFirst(String name) {
                    if (PARENT_FIRST_CLASSES.contains(name)) return true;
                    for (String prefix : PARENT_FIRST_PREFIXES) {
                        if (name.startsWith(prefix)) return true;
                    }
                    return false;
                }

                @Override
                public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                    synchronized (getClassLoadingLock(name)) {
                        // 1. Check if already loaded
                        Class<?> c = findLoadedClass(name);
                        if (c != null) {
                            if (resolve) resolveClass(c);
                            return c;
                        }

                        // 2. Parent-first for framework/JDK packages
                        if (isParentFirst(name)) {
                            return super.loadClass(name, resolve);
                        }

                        // 3. Child-first: try our URLs first (mod classes)
                        try {
                            c = findClass(name);
                            if (resolve) resolveClass(c);
                            return c;
                        } catch (ClassNotFoundException ignored) {}

                        // 4. Fall back to parent
                        return super.loadClass(name, resolve);
                    }
                }

                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    Thread.currentThread().setContextClassLoader(this);
                    return super.findClass(name);
                }
            };
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to create classloader", e);
            return null;
        }
    }
}
