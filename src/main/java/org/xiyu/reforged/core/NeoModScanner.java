package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import org.objectweb.asm.*;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * ASM-based scanner for NeoForge mod JARs.
 *
 * <p>Provides two levels of scanning:</p>
 * <ul>
 *   <li>{@link #scanForModClasses(Path)} — lightweight scan for {@code @Mod} annotated classes</li>
 *   <li>{@link #scanJarAnnotations(Path)} — full scan collecting all annotations and class hierarchy
 *       into a Forge {@link net.minecraftforge.forgespi.language.ModFileScanData}</li>
 * </ul>
 */
public final class NeoModScanner {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NEO_MOD_ANNOTATION = "Lnet/neoforged/fml/common/Mod;";

    private NeoModScanner() {}

    /** Info about a discovered @Mod annotated class. */
    public record ModInfo(String modId, String className) {}

    /**
     * Use ASM to scan a JAR for classes annotated with @net.neoforged.fml.common.Mod.
     */
    public static List<ModInfo> scanForModClasses(Path jarPath) {
        List<ModInfo> result = new ArrayList<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                if (entry.getName().startsWith("META-INF/")) continue;

                try (InputStream is = jar.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(is);
                    ModAnnotationScanner scanner = new ModAnnotationScanner();
                    reader.accept(scanner, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);

                    if (scanner.modId != null) {
                        String className = entry.getName()
                                .replace('/', '.')
                                .replace(".class", "");
                        result.add(new ModInfo(scanner.modId, className));
                    }
                } catch (Exception e) {
                    // Skip unreadable class files
                }
            }
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to scan JAR: {}", jarPath.getFileName(), e);
        }
        return result;
    }

    /**
     * Perform a full ASM scan of a JAR, collecting ALL annotation data and class hierarchy
     * information into a Forge {@link net.minecraftforge.forgespi.language.ModFileScanData}.
     *
     * <p>This allows NeoForge mods (like Twilight Forest's BeanContext) to discover their
     * annotated classes (e.g. {@code @BeanProcessor}, {@code @Bean}, {@code @Component})
     * through the standard {@code ModFileScanData.getAnnotatedBy()} / {@code getAnnotations()} APIs.</p>
     */
    public static net.minecraftforge.forgespi.language.ModFileScanData scanJarAnnotations(Path jarPath) {
        net.minecraftforge.forgespi.language.ModFileScanData scanData =
                new net.minecraftforge.forgespi.language.ModFileScanData();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                if (entry.getName().startsWith("META-INF/")) continue;

                try (InputStream is = jar.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(is);
                    FullAnnotationScanner scanner = new FullAnnotationScanner(scanData);
                    reader.accept(scanner, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
                } catch (Exception e) {
                    // Skip unreadable class files
                }
            }
        } catch (Exception e) {
            LOGGER.error("[ReForged] Failed to scan JAR annotations: {}", jarPath.getFileName(), e);
        }
        return scanData;
    }

    // ═══════════════════════════════════════════════════════════
    //  Internal ASM visitors
    // ═══════════════════════════════════════════════════════════

    /**
     * ASM visitor that scans for @Mod annotation and extracts the modId.
     */
    private static class ModAnnotationScanner extends ClassVisitor {
        String modId = null;

        ModAnnotationScanner() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (NEO_MOD_ANNOTATION.equals(descriptor)) {
                return new AnnotationVisitor(Opcodes.ASM9) {
                    @Override
                    public void visit(String name, Object value) {
                        if ("value".equals(name) && value instanceof String s) {
                            modId = s;
                        }
                    }
                };
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }

    /**
     * ASM visitor that collects ALL annotations (class-level, method-level, field-level)
     * and class hierarchy data into a Forge ModFileScanData.
     *
     * <p>This mirrors what NeoForge's ModFile scanner does, allowing third-party mods
     * that rely on annotation scanning (like Twilight Forest's beanification framework)
     * to work correctly under ReForged.</p>
     */
    private static class FullAnnotationScanner extends ClassVisitor {
        private final net.minecraftforge.forgespi.language.ModFileScanData scanData;
        private Type classType;

        FullAnnotationScanner(net.minecraftforge.forgespi.language.ModFileScanData scanData) {
            super(Opcodes.ASM9);
            this.scanData = scanData;
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            this.classType = Type.getObjectType(name);
            Type parentType = superName != null ? Type.getObjectType(superName) : Type.getType(Object.class);
            Set<Type> ifaceTypes = new LinkedHashSet<>();
            if (interfaces != null) {
                for (String iface : interfaces) {
                    ifaceTypes.add(Type.getObjectType(iface));
                }
            }
            scanData.getClasses().add(
                    new net.minecraftforge.forgespi.language.ModFileScanData.ClassData(
                            classType, parentType, ifaceTypes));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            return new ScanAnnotationVisitor(descriptor, java.lang.annotation.ElementType.TYPE,
                    classType, classType.getClassName(), scanData);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor,
                                        String signature, Object value) {
            return new FieldVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    return new ScanAnnotationVisitor(desc, java.lang.annotation.ElementType.FIELD,
                            classType, name, scanData);
                }
            };
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                          String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    return new ScanAnnotationVisitor(desc, java.lang.annotation.ElementType.METHOD,
                            classType, name, scanData);
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
                    return new ScanAnnotationVisitor(desc, java.lang.annotation.ElementType.PARAMETER,
                            classType, name, scanData);
                }
            };
        }
    }

    /**
     * ASM AnnotationVisitor that collects annotation data (including nested values,
     * arrays, and enum constants) into a Map, then adds it to the scan data.
     */
    private static class ScanAnnotationVisitor extends AnnotationVisitor {
        private final Type annotationType;
        private final java.lang.annotation.ElementType targetType;
        private final Type classType;
        private final String memberName;
        private final net.minecraftforge.forgespi.language.ModFileScanData scanData;
        private final Map<String, Object> values = new LinkedHashMap<>();

        ScanAnnotationVisitor(String descriptor, java.lang.annotation.ElementType targetType,
                              Type classType, String memberName,
                              net.minecraftforge.forgespi.language.ModFileScanData scanData) {
            super(Opcodes.ASM9);
            this.annotationType = Type.getType(descriptor);
            this.targetType = targetType;
            this.classType = classType;
            this.memberName = memberName;
            this.scanData = scanData;
        }

        @Override
        public void visit(String name, Object value) {
            values.put(name, value);
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            values.put(name, new net.neoforged.fml.loading.modscan.ModAnnotation.EnumHolder(descriptor, value));
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            List<Object> list = new ArrayList<>();
            values.put(name, list);
            return new AnnotationVisitor(Opcodes.ASM9) {
                @Override
                public void visit(String n, Object value) {
                    list.add(value);
                }

                @Override
                public void visitEnum(String n, String desc, String value) {
                    list.add(new net.neoforged.fml.loading.modscan.ModAnnotation.EnumHolder(desc, value));
                }
            };
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            // Nested annotations — store as a map
            Map<String, Object> nested = new LinkedHashMap<>();
            values.put(name, nested);
            return new AnnotationVisitor(Opcodes.ASM9) {
                @Override
                public void visit(String n, Object value) {
                    nested.put(n, value);
                }

                @Override
                public void visitEnum(String n, String desc, String value) {
                    nested.put(n, new net.neoforged.fml.loading.modscan.ModAnnotation.EnumHolder(desc, value));
                }
            };
        }

        @Override
        public void visitEnd() {
            scanData.getAnnotations().add(
                    new net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData(
                            annotationType, targetType, classType, memberName, values));
        }
    }
}
