package org.xiyu.reforged.asm;

import com.mojang.logging.LogUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.slf4j.Logger;

/**
 * ModConstructorTransformer — Rewrites {@code @Mod}-annotated class constructors
 * from NeoForge's signature to Forge's signature.
 *
 * <h3>Supported NeoForge constructor patterns</h3>
 * <table>
 *     <tr><th>NeoForge</th><th>Forge (after transform)</th></tr>
 *     <tr><td>{@code MyMod(IEventBus)}</td><td>{@code MyMod(FMLJavaModLoadingContext)}</td></tr>
 *     <tr><td>{@code MyMod(IEventBus, ModContainer)}</td><td>{@code MyMod(FMLJavaModLoadingContext)}</td></tr>
 *     <tr><td>{@code MyMod(ModContainer)}</td><td>{@code MyMod(FMLJavaModLoadingContext)}</td></tr>
 *     <tr><td>{@code MyMod()}</td><td>no change</td></tr>
 * </table>
 *
 * <h3>Technique</h3>
 * <p>After the {@code super()} call, inject bytecode to extract the IEventBus
 * (and optionally the ModContainer) from FMLJavaModLoadingContext, overwriting
 * the parameter slots so all original code works unchanged.</p>
 */
public final class ModConstructorTransformer extends ClassVisitor {

    private static final Logger LOGGER = LogUtils.getLogger();

    // After package remapping, these are the types we look for
    private static final String IEVENTBUS_INT = "net/minecraftforge/eventbus/api/IEventBus";
    private static final String IEVENTBUS_DESC = "L" + IEVENTBUS_INT + ";";
    private static final String MOD_CONTAINER_INT = "net/minecraftforge/fml/ModContainer";
    private static final String MOD_CONTAINER_DESC = "L" + MOD_CONTAINER_INT + ";";
    private static final String CONTEXT_INT = "net/minecraftforge/fml/javafmlmod/FMLJavaModLoadingContext";
    private static final String CONTEXT_DESC = "L" + CONTEXT_INT + ";";

    // Constructor descriptor patterns
    private static final String PATTERN_BUS_ONLY = "(" + IEVENTBUS_DESC + ")V";
    private static final String PATTERN_BUS_CONTAINER = "(" + IEVENTBUS_DESC + MOD_CONTAINER_DESC + ")V";
    private static final String PATTERN_CONTAINER_ONLY = "(" + MOD_CONTAINER_DESC + ")V";
    private static final String TARGET_DESC = "(" + CONTEXT_DESC + ")V";

    private boolean hasModAnnotation = false;
    private String className = "unknown";

    public ModConstructorTransformer(ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if ("Lnet/minecraftforge/fml/common/Mod;".equals(descriptor)) {
            hasModAnnotation = true;
            LOGGER.debug("[ReForged] ModConstructorTransformer: Found @Mod on {}", className);
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        if (!hasModAnnotation || !"<init>".equals(name)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        // Pattern 1: (IEventBus)V → (FMLJavaModLoadingContext)V
        if (PATTERN_BUS_ONLY.equals(descriptor)) {
            LOGGER.info("[ReForged] Rewriting {}<init>(IEventBus)V → (FMLJavaModLoadingContext)V", className);
            MethodVisitor mv = super.visitMethod(access, name, TARGET_DESC, signature, exceptions);
            return new BusOnlyRewriter(mv, access, className);
        }

        // Pattern 2: (IEventBus, ModContainer)V → (FMLJavaModLoadingContext)V
        if (PATTERN_BUS_CONTAINER.equals(descriptor)) {
            LOGGER.info("[ReForged] Rewriting {}<init>(IEventBus,ModContainer)V → (FMLJavaModLoadingContext)V", className);
            MethodVisitor mv = super.visitMethod(access, name, TARGET_DESC, signature, exceptions);
            return new BusContainerRewriter(mv, access, className);
        }

        // Pattern 3: (ModContainer)V → (FMLJavaModLoadingContext)V
        if (PATTERN_CONTAINER_ONLY.equals(descriptor)) {
            LOGGER.info("[ReForged] Rewriting {}<init>(ModContainer)V → (FMLJavaModLoadingContext)V", className);
            MethodVisitor mv = super.visitMethod(access, name, TARGET_DESC, signature, exceptions);
            return new ContainerOnlyRewriter(mv, access, className);
        }

        // Pattern 4: ()V → no change
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    // ─── Pattern 1: (IEventBus)V ───────────────────────────────

    private static final class BusOnlyRewriter extends AdviceAdapter {
        private final String className;

        BusOnlyRewriter(MethodVisitor mv, int access, String className) {
            super(Opcodes.ASM9, mv, access, "<init>", "(" + CONTEXT_DESC + ")V");
            this.className = className;
        }

        @Override
        protected void onMethodEnter() {
            // slot 1 = FMLJavaModLoadingContext
            // Extract IEventBus and store in slot 1 (overwrite context)
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CONTEXT_INT,
                    "getModEventBus", "()" + IEVENTBUS_DESC, false);
            mv.visitVarInsn(Opcodes.ASTORE, 1);
            // Now slot 1 = IEventBus, all original code works
        }
    }

    // ─── Pattern 2: (IEventBus, ModContainer)V ─────────────────

    private static final class BusContainerRewriter extends AdviceAdapter {
        private final String className;

        BusContainerRewriter(MethodVisitor mv, int access, String className) {
            super(Opcodes.ASM9, mv, access, "<init>", "(" + CONTEXT_DESC + ")V");
            this.className = className;
        }

        @Override
        protected void onMethodEnter() {
            // slot 1 = FMLJavaModLoadingContext
            // We need to produce: slot 1 = IEventBus, slot 2 = ModContainer
            // But Forge's FMLJavaModLoadingContext doesn't expose ModContainer directly.
            // We'll get the bus and store a null placeholder for ModContainer.

            // First, save context reference
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ASTORE, 3); // save context in slot 3

            // Extract IEventBus → slot 1
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CONTEXT_INT,
                    "getModEventBus", "()" + IEVENTBUS_DESC, false);
            mv.visitVarInsn(Opcodes.ASTORE, 1);

            // Get ModContainer from ModLoadingContext → slot 2
            // FMLJavaModLoadingContext extends ModLoadingContext which has getActiveContainer()
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "net/minecraftforge/fml/ModLoadingContext", "get",
                    "()Lnet/minecraftforge/fml/ModLoadingContext;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "net/minecraftforge/fml/ModLoadingContext", "getActiveContainer",
                    "()" + MOD_CONTAINER_DESC, false);
            mv.visitVarInsn(Opcodes.ASTORE, 2);
            // Now slot 1 = IEventBus, slot 2 = ModContainer
        }
    }

    // ─── Pattern 3: (ModContainer)V ────────────────────────────

    private static final class ContainerOnlyRewriter extends AdviceAdapter {
        private final String className;

        ContainerOnlyRewriter(MethodVisitor mv, int access, String className) {
            super(Opcodes.ASM9, mv, access, "<init>", "(" + CONTEXT_DESC + ")V");
            this.className = className;
        }

        @Override
        protected void onMethodEnter() {
            // slot 1 = FMLJavaModLoadingContext
            // Need: slot 1 = ModContainer
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "net/minecraftforge/fml/ModLoadingContext", "get",
                    "()Lnet/minecraftforge/fml/ModLoadingContext;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "net/minecraftforge/fml/ModLoadingContext", "getActiveContainer",
                    "()" + MOD_CONTAINER_DESC, false);
            mv.visitVarInsn(Opcodes.ASTORE, 1);
        }
    }
}
