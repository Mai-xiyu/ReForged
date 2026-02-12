package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

/**
 * ReForgedPreProcessor — Standalone entry point for pre-processing NeoForge mod JARs.
 *
 * <h3>Usage</h3>
 * <pre>
 * java -cp ... org.xiyu.reforged.core.ReForgedPreProcessor &lt;inputDir&gt; &lt;outputDir&gt;
 * </pre>
 *
 * <p>Or via the Gradle task:
 * <pre>
 * gradlew processNeoMods
 * </pre>
 *
 * <p>Scans {@code inputDir} (default: {@code neoforge-mods/}) for {@code .jar} files,
 * rewrites each through {@link JarRewriter}, and outputs Forge-compatible JARs to
 * {@code outputDir} (default: {@code run/mods/}).
 */
public final class ReForgedPreProcessor {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void main(String[] args) {
        Path inputDir = args.length > 0 ? Path.of(args[0]) : Path.of("neoforge-mods");
        Path outputDir = args.length > 1 ? Path.of(args[1]) : Path.of("run", "mods");

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  ReForged Pre-Processor                     ║");
        System.out.println("║  NeoForge → Forge JAR Converter             ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println("  Input:  " + inputDir.toAbsolutePath());
        System.out.println("  Output: " + outputDir.toAbsolutePath());
        System.out.println();

        if (!Files.isDirectory(inputDir)) {
            System.err.println("[ReForged] Input directory does not exist: " + inputDir);
            System.err.println("[ReForged] Create it and place NeoForge mod JARs inside.");
            System.exit(1);
        }

        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            System.err.println("[ReForged] Cannot create output directory: " + e.getMessage());
            System.exit(1);
        }

        JarRewriter rewriter = new JarRewriter();
        int success = 0;
        int failed = 0;

        try (Stream<Path> files = Files.list(inputDir)) {
            for (Path jar : files.filter(p -> p.toString().endsWith(".jar")).toList()) {
                String outputName = jar.getFileName().toString()
                        .replace(".jar", ".reforged.jar");
                Path outputJar = outputDir.resolve(outputName);

                System.out.println("[ReForged] Processing: " + jar.getFileName());

                if (rewriter.rewrite(jar, outputJar)) {
                    System.out.println("[ReForged]   ✓ → " + outputJar.getFileName());
                    success++;
                } else {
                    System.err.println("[ReForged]   ✗ Failed: " + jar.getFileName());
                    failed++;
                }
            }
        } catch (IOException e) {
            System.err.println("[ReForged] Error scanning input directory: " + e.getMessage());
            System.exit(1);
        }

        System.out.println();
        System.out.println("[ReForged] Done: " + success + " succeeded, " + failed + " failed");
        if (success > 0) {
            System.out.println("[ReForged] Rewritten JARs are in: " + outputDir.toAbsolutePath());
            System.out.println("[ReForged] Run 'gradlew runClient' to test them.");
        }

        System.exit(failed > 0 ? 1 : 0);
    }

    private ReForgedPreProcessor() {}
}
