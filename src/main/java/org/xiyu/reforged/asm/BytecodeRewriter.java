package org.xiyu.reforged.asm;

import com.mojang.logging.LogUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.slf4j.Logger;

/**
 * BytecodeRewriter — The main entry point for transforming NeoForge bytecode.
 *
 * <p>Given raw class bytes from a NeoForge mod JAR, this class uses an ASM
 * {@link ClassRemapper} pipeline with {@link ReForgedRemapper} to rewrite all
 * references from {@code net.neoforged.*} to either the equivalent
 * {@code net.minecraftforge.*} class or one of our shim classes in
 * {@code org.xiyu.reforged.shim.*}.
 *
 * <h3>What gets rewritten:</h3>
 * <ul>
 *     <li>Class inheritance ({@code extends / implements})</li>
 *     <li>Field types and method parameter/return types (descriptors)</li>
 *     <li>Generic signatures</li>
 *     <li>Annotation class references and values</li>
 *     <li>Constant-pool class entries</li>
 *     <li>LDC string constants containing internal class names</li>
 * </ul>
 */
public final class BytecodeRewriter {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ReForgedRemapper remapper;

    public BytecodeRewriter() {
        this.remapper = new ReForgedRemapper();
    }

    public BytecodeRewriter(MappingRegistry registry) {
        this.remapper = new ReForgedRemapper(registry);
    }

    /**
     * Rewrite a single class file's bytecode.
     *
     * @param originalBytes the raw bytes of the {@code .class} file
     * @return the transformed bytes with all NeoForge references remapped
     */
    public byte[] rewrite(byte[] originalBytes) {
        try {
            ClassReader reader = new ClassReader(originalBytes);
            // COMPUTE_MAXS preserves existing stack frames while recalculating max stack/locals.
            // We avoid COMPUTE_FRAMES because it requires the full type hierarchy on the classpath,
            // which is not available during offline JAR rewriting.
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassVisitor visitor = new ClassRemapper(writer, remapper);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);

            LOGGER.debug("[ReForged] Rewrote class: {}", reader.getClassName());
            return writer.toByteArray();

        } catch (Exception e) {
            // If rewriting fails, return original bytes and log the error so the mod
            // can still attempt to load (it will likely fail later with a clearer error).
            LOGGER.error("[ReForged] Failed to rewrite class, returning original bytes", e);
            return originalBytes;
        }
    }

    /**
     * Check whether a class file contains any references that need remapping.
     * Useful for skipping the expensive rewrite pass on classes that don't
     * reference NeoForge at all.
     *
     * @param originalBytes the raw bytes of the {@code .class} file
     * @return {@code true} if the class references any {@code net/neoforged/} types
     */
    public boolean needsRewrite(byte[] originalBytes) {
        try {
            ClassReader reader = new ClassReader(originalBytes);
            String className = reader.getClassName();
            // Quick check: does the class name itself or its superclass/interfaces start with net/neoforged/?
            if (className.startsWith("net/neoforged/")) return true;
            String superName = reader.getSuperName();
            if (superName != null && superName.startsWith("net/neoforged/")) return true;
            for (String iface : reader.getInterfaces()) {
                if (iface.startsWith("net/neoforged/")) return true;
            }
            // For a thorough check we'd scan the constant pool; for the skeleton we rely on
            // the simple heuristic above plus always-rewriting classes from NeoForge mod JARs.
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
