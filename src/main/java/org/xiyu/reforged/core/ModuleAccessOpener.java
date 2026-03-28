package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Open all packages from game-layer modules (minecraft, forge, etc.) to the
 * unnamed module of a given classloader.
 *
 * <p><b>Why this is needed:</b> NeoForge mod classes are loaded by a
 * {@link URLClassLoader} and end up in its <em>unnamed</em> module. Minecraft
 * and Forge classes live in <em>named</em> JPMS modules ({@code minecraft@1.21.1},
 * {@code forge}, etc.). The JVM blocks cross-module access unless the named
 * module explicitly exports its packages to the reader module.</p>
 *
 * <p>We use the same technique as Forge's own {@code ForgeMod.addOpen()}:
 * reflective access to {@code Module.implAddExportsOrOpens} via
 * {@code sun.misc.Unsafe} to bypass module encapsulation.</p>
 */
public final class ModuleAccessOpener {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ModuleAccessOpener() {}

    /**
     * Open all packages from game-layer modules to the unnamed module of the given classloader.
     *
     * @param neoClassLoader the URLClassLoader whose unnamed module needs access
     * @param referenceClass a class from the game classloader (used to locate modules)
     */
    @SuppressWarnings("deprecation") // Unsafe.objectFieldOffset is deprecated since JDK 18 but still functional
    public static void openGameModulesToClassLoader(URLClassLoader neoClassLoader, Class<?> referenceClass) {
        try {
            // ── Step 1: Obtain sun.misc.Unsafe ───────────────────────────────
            Field theUnsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafeField.get(null);

            // ── Step 2: Find Class.module field offset via Unsafe ────────────
            long moduleFieldOffset = -1;
            for (Field f : Class.class.getDeclaredFields()) {
                if ("module".equals(f.getName())) {
                    moduleFieldOffset = unsafe.objectFieldOffset(f);
                    break;
                }
            }
            if (moduleFieldOffset == -1) {
                LOGGER.error("[ReForged] Cannot find 'module' field in java.lang.Class — " +
                        "module access hack unavailable, NeoForge mods may crash");
                return;
            }

            // ── Step 3: Get Module.implAddExportsOrOpens reflectively ────────
            // We must bypass setAccessible() module checks by temporarily
            // pretending the CALLING class (ModuleAccessOpener) belongs to
            // java.base. The JVM's setAccessible() check uses the caller's
            // module, not the target class's module.
            Object javaBaseModule = unsafe.getObject(Object.class, moduleFieldOffset); // Object.class → java.base
            Object ourOriginalModule = unsafe.getObject(ModuleAccessOpener.class, moduleFieldOffset);
            unsafe.putObject(ModuleAccessOpener.class, moduleFieldOffset, javaBaseModule);

            Method implAddExportsOrOpens;
            try {
                implAddExportsOrOpens = Module.class.getDeclaredMethod(
                        "implAddExportsOrOpens",
                        String.class,   // package name
                        Module.class,   // target module
                        boolean.class,  // open (true) vs export-only (false)
                        boolean.class   // syncVM
                );
                implAddExportsOrOpens.setAccessible(true);
            } finally {
                // Restore original module immediately
                unsafe.putObject(ModuleAccessOpener.class, moduleFieldOffset, ourOriginalModule);
            }

            // ── Step 4: Collect all named modules from game / parent layers ──
            Module unnamedModule = neoClassLoader.getUnnamedModule();
            ClassLoader gameClassLoader = referenceClass.getClassLoader();

            Set<Module> modulesToOpen = new LinkedHashSet<>();

            // Try to discover the game ModuleLayer by loading a known MC class
            String[] probeClasses = {
                    "net.minecraft.world.entity.Entity",       // minecraft module
                    "net.minecraftforge.common.MinecraftForge", // forge module
                    "net.minecraftforge.fml.ModList",           // fml module
            };
            for (String probeName : probeClasses) {
                try {
                    Class<?> probe = Class.forName(probeName, false, gameClassLoader);
                    Module mod = probe.getModule();
                    if (mod.isNamed()) {
                        modulesToOpen.add(mod);
                        // Also grab all sibling modules from the same layer
                        ModuleLayer layer = mod.getLayer();
                        if (layer != null) {
                            for (Module sibling : layer.modules()) {
                                if (sibling.isNamed()) {
                                    modulesToOpen.add(sibling);
                                }
                            }
                        }
                    }
                } catch (ClassNotFoundException ignored) {
                    // Probe class not available — skip
                }
            }

            if (modulesToOpen.isEmpty()) {
                LOGGER.warn("[ReForged] No named game modules found — " +
                        "module access hack skipped (classes may already be in unnamed module)");
                return;
            }

            // ── Step 5: Open every package of every game module ──────────────
            int totalPackages = 0;
            for (Module module : modulesToOpen) {
                Set<String> packages = module.getPackages();
                for (String pkg : packages) {
                    try {
                        implAddExportsOrOpens.invoke(
                                module,
                                pkg,
                                unnamedModule,
                                /* open = */ true,
                                /* syncVM = */ true
                        );
                    } catch (Exception e) {
                        LOGGER.debug("[ReForged] Failed to open {}/{} — {}",
                                module.getName(), pkg, e.getMessage());
                    }
                }
                totalPackages += packages.size();
            }

            LOGGER.info("[ReForged] Opened {} packages across {} modules to NeoForge classloader's unnamed module",
                    totalPackages, modulesToOpen.size());

        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to open game module packages — " +
                    "NeoForge mods extending Minecraft classes will likely crash with IllegalAccessError", e);
        }
    }
}
