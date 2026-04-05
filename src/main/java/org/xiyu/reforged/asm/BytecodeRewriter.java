package org.xiyu.reforged.asm;

import com.mojang.logging.LogUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

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
            // Chain: reader → ClassRemapper → MethodCallRedirector → writer
            // ClassRemapper rewrites class references (net.neoforged → net.minecraftforge),
            // then MethodCallRedirector intercepts specific method calls that need
            // special handling (e.g. IEventBus.post descriptor mismatch).
            ClassVisitor redirector = new MethodCallRedirector(writer);
            ClassVisitor visitor = new ClassRemapper(redirector, remapper);
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

    /**
     * Intercepts specific method calls in the bytecode and redirects them.
     *
     * <p>After ClassRemapper rewrites class references, some method descriptors
     * end up mismatched (e.g. {@code IEventBus.post(Event)→Event} vs the inherited
     * {@code IEventBus.post(Event)→boolean}). This visitor redirects such calls
     * to static helper methods that handle the adaptation.</p>
     */
    private static final class MethodCallRedirector extends ClassVisitor {

        private static final String ENTITY_TYPE_EXT =
                "dev/engine_room/flywheel/impl/extension/EntityTypeExtension";
        private static final String BLOCK_ENTITY_TYPE_EXT =
                "dev/engine_room/flywheel/impl/extension/BlockEntityTypeExtension";
        private static final String BRIDGE =
                "org/xiyu/reforged/bridge/FlywheelVisualizerBridge";

        // ── Create mixin accessor → MC target class mapping ────────────
        // Create's NeoForge Mixin config is not loaded by Forge's Sponge Mixin
        // system, so accessor interfaces are never injected.  We inject method
        // bodies via our own Mixins and rewrite INVOKEINTERFACE → INVOKEVIRTUAL.
        private static final String CREATE_ACCESSOR_PREFIX =
                "com/simibubi/create/foundation/mixin/accessor/";
        private static final Map<String, String> CREATE_ACCESSOR_TARGETS = new HashMap<>();

        // ── Flywheel mixin accessor/extension → MC target class mapping ─────
        // Same situation as Create: Flywheel's mixins are loaded by NeoModClassLoader
        // (a child classloader), invisible to the Mixin framework running in TRANSFORMER.
        private static final Map<String, String> FLYWHEEL_ACCESSOR_TARGETS = new HashMap<>();

        // Combined lookup for CHECKCAST/frame replacement (both Create + Flywheel)
        private static final Map<String, String> ALL_ACCESSOR_TARGETS = new HashMap<>();
        static {
            String P = CREATE_ACCESSOR_PREFIX;
            CREATE_ACCESSOR_TARGETS.put(P + "StateHolderAccessor",              "net/minecraft/world/level/block/state/StateHolder");
            CREATE_ACCESSOR_TARGETS.put(P + "ConcretePowderBlockAccessor",      "net/minecraft/world/level/block/ConcretePowderBlock");
            CREATE_ACCESSOR_TARGETS.put(P + "HumanoidArmorLayerAccessor",       "net/minecraft/client/renderer/entity/layers/HumanoidArmorLayer");
            CREATE_ACCESSOR_TARGETS.put(P + "UseOnContextAccessor",             "net/minecraft/world/item/context/UseOnContext");
            CREATE_ACCESSOR_TARGETS.put(P + "BlockLootSubProviderAccessor",     "net/minecraft/data/loot/BlockLootSubProvider");
            CREATE_ACCESSOR_TARGETS.put(P + "GuiAccessor",                      "net/minecraft/client/gui/Gui");
            CREATE_ACCESSOR_TARGETS.put(P + "GameTestHelperAccessor",           "net/minecraft/gametest/framework/GameTestHelper");
            CREATE_ACCESSOR_TARGETS.put(P + "EntityRenderDispatcherAccessor",   "net/minecraft/client/renderer/entity/EntityRenderDispatcher");
            CREATE_ACCESSOR_TARGETS.put(P + "MouseHandlerAccessor",             "net/minecraft/client/MouseHandler");
            CREATE_ACCESSOR_TARGETS.put(P + "MinecraftAccessor",                "net/minecraft/client/Minecraft");
            CREATE_ACCESSOR_TARGETS.put(P + "CropBlockAccessor",                "net/minecraft/world/level/block/CropBlock");
            CREATE_ACCESSOR_TARGETS.put(P + "TimerAccessor",                    "net/minecraft/client/DeltaTracker$Timer");
            CREATE_ACCESSOR_TARGETS.put(P + "SystemReportAccessor",             "net/minecraft/SystemReport");
            CREATE_ACCESSOR_TARGETS.put(P + "ItemStackHandlerAccessor",         "net/minecraftforge/items/ItemStackHandler");
            CREATE_ACCESSOR_TARGETS.put(P + "MinecartFurnaceAccessor",          "net/minecraft/world/entity/vehicle/MinecartFurnace");
            CREATE_ACCESSOR_TARGETS.put(P + "ServerLevelAccessor",              "net/minecraft/server/level/ServerLevel");
            CREATE_ACCESSOR_TARGETS.put(P + "LivingEntityAccessor",             "net/minecraft/world/entity/LivingEntity");
            CREATE_ACCESSOR_TARGETS.put(P + "ShapedRecipeAccessor",             "net/minecraft/world/item/crafting/ShapedRecipe");
            CREATE_ACCESSOR_TARGETS.put(P + "ProjectileDispenseBehaviorAccessor","net/minecraft/core/dispenser/ProjectileDispenseBehavior");
            CREATE_ACCESSOR_TARGETS.put(P + "BlockBehaviourAccessor",           "net/minecraft/world/level/block/state/BlockBehaviour");
            CREATE_ACCESSOR_TARGETS.put(P + "MappedRegistryAccessor",           "net/minecraft/core/MappedRegistry");
            CREATE_ACCESSOR_TARGETS.put(P + "FlowingFluidAccessor",             "net/minecraft/world/level/material/FlowingFluid");
            CREATE_ACCESSOR_TARGETS.put(P + "PotionBrewingAccessor",            "net/minecraft/world/item/alchemy/PotionBrewing");
            CREATE_ACCESSOR_TARGETS.put(P + "ItemFrameAccessor",                "net/minecraft/world/entity/decoration/ItemFrame");
            CREATE_ACCESSOR_TARGETS.put(P + "MobEffectInstanceAccessor",        "net/minecraft/world/effect/MobEffectInstance");
            CREATE_ACCESSOR_TARGETS.put(P + "DispenserBlockAccessor",           "net/minecraft/world/level/block/DispenserBlock");
            CREATE_ACCESSOR_TARGETS.put(P + "LevelRendererAccessor",            "net/minecraft/client/renderer/LevelRenderer");
            CREATE_ACCESSOR_TARGETS.put(P + "AgeableListModelAccessor",         "net/minecraft/client/model/AgeableListModel");
            CREATE_ACCESSOR_TARGETS.put(P + "FontAccessor",                     "net/minecraft/client/gui/Font");
            CREATE_ACCESSOR_TARGETS.put(P + "NbtAccounterAccessor",             "net/minecraft/nbt/NbtAccounter");
            // Empty interfaces (still need CHECKCAST removal):
            CREATE_ACCESSOR_TARGETS.put(P + "ItemModelGeneratorsAccessor",      "net/minecraft/data/models/ItemModelGenerators");
            CREATE_ACCESSOR_TARGETS.put(P + "FallingBlockEntityAccessor",       "net/minecraft/world/entity/item/FallingBlockEntity");
            CREATE_ACCESSOR_TARGETS.put(P + "FluidInteractionRegistryAccessor", "net/neoforged/neoforge/fluids/FluidInteractionRegistry");

            // ── Flywheel accessor interfaces ─────────────────────────────
            // backend/mixin/
            FLYWHEEL_ACCESSOR_TARGETS.put("dev/engine_room/flywheel/backend/mixin/LevelRendererAccessor",
                    "net/minecraft/client/renderer/LevelRenderer");
            FLYWHEEL_ACCESSOR_TARGETS.put("dev/engine_room/flywheel/backend/mixin/AbstractClientPlayerAccessor",
                    "net/minecraft/client/player/AbstractClientPlayer");
            // backend/mixin/light/
            FLYWHEEL_ACCESSOR_TARGETS.put("dev/engine_room/flywheel/backend/mixin/light/LayerLightSectionStorageAccessor",
                    "net/minecraft/world/level/lighting/LayerLightSectionStorage");
            FLYWHEEL_ACCESSOR_TARGETS.put("dev/engine_room/flywheel/backend/mixin/light/LightEngineAccessor",
                    "net/minecraft/world/level/lighting/LightEngine");
            FLYWHEEL_ACCESSOR_TARGETS.put("dev/engine_room/flywheel/backend/mixin/light/SkyDataLayerStorageMapAccessor",
                    "net/minecraft/world/level/lighting/SkyLightSectionStorage$SkyDataLayerStorageMap");
            // impl/mixin/
            FLYWHEEL_ACCESSOR_TARGETS.put("dev/engine_room/flywheel/impl/mixin/ModelPartAccessor",
                    "net/minecraft/client/model/geom/ModelPart");
            FLYWHEEL_ACCESSOR_TARGETS.put("dev/engine_room/flywheel/impl/mixin/PoseStackAccessor",
                    "com/mojang/blaze3d/vertex/PoseStack");
            // ── Flywheel extension interfaces (duck-typing) ──────────────
            FLYWHEEL_ACCESSOR_TARGETS.put("dev/engine_room/flywheel/impl/extension/LevelExtension",
                    "net/minecraft/world/level/Level");
            FLYWHEEL_ACCESSOR_TARGETS.put("dev/engine_room/flywheel/impl/extension/PoseStackExtension",
                    "com/mojang/blaze3d/vertex/PoseStack");
            FLYWHEEL_ACCESSOR_TARGETS.put("dev/engine_room/flywheel/backend/SkyLightSectionStorageExtension",
                    "net/minecraft/world/level/lighting/SkyLightSectionStorage");

            // ── Build combined lookup map ────────────────────────────────
            ALL_ACCESSOR_TARGETS.putAll(CREATE_ACCESSOR_TARGETS);
            ALL_ACCESSOR_TARGETS.putAll(FLYWHEEL_ACCESSOR_TARGETS);
        }

        /**
         * Rewrite accessor interface types to their MC target class types in descriptors.
         * E.g. {@code (Ldev/engine_room/flywheel/backend/mixin/light/LayerLightSectionStorageAccessor;J)V}
         * becomes {@code (Lnet/minecraft/world/level/lighting/LayerLightSectionStorage;J)V}.
         */
        private static String rewriteAccessorDescriptor(String descriptor) {
            if (descriptor == null) return null;
            String result = descriptor;
            for (Map.Entry<String, String> e : ALL_ACCESSOR_TARGETS.entrySet()) {
                String from = "L" + e.getKey() + ";";
                if (result.contains(from)) {
                    result = result.replace(from, "L" + e.getValue() + ";");
                }
            }
            return result;
        }

        /**
         * Rewrite accessor types in INVOKEDYNAMIC bootstrap method args (for lambdas).
         */
        private static Object[] rewriteBootstrapArgs(Object[] bsmArgs) {
            Object[] result = new Object[bsmArgs.length];
            for (int i = 0; i < bsmArgs.length; i++) {
                Object arg = bsmArgs[i];
                if (arg instanceof Handle h) {
                    String hDesc = rewriteAccessorDescriptor(h.getDesc());
                    if (!hDesc.equals(h.getDesc())) {
                        result[i] = new Handle(h.getTag(), h.getOwner(), h.getName(), hDesc, h.isInterface());
                    } else {
                        result[i] = arg;
                    }
                } else if (arg instanceof Type t && t.getSort() == Type.METHOD) {
                    String tDesc = rewriteAccessorDescriptor(t.getDescriptor());
                    if (!tDesc.equals(t.getDescriptor())) {
                        result[i] = Type.getMethodType(tDesc);
                    } else {
                        result[i] = arg;
                    }
                } else {
                    result[i] = arg;
                }
            }
            return result;
        }

        MethodCallRedirector(ClassVisitor cv) {
            super(Opcodes.ASM9, cv);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor,
                                       String signature, Object value) {
            return super.visitField(access, name, rewriteAccessorDescriptor(descriptor),
                                    signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            String newDesc = rewriteAccessorDescriptor(descriptor);
            MethodVisitor mv = super.visitMethod(access, name, newDesc, signature, exceptions);
            return new MethodVisitor(Opcodes.ASM9, mv) {

                // ── Replace Create accessor types in stack map frames ──────
                // When the original code stores a casted value in a local variable
                // typed as an accessor interface, the stack frame records that type.
                // We must replace it with the target MC class type to avoid
                // VerifyError ("is not assignable to").
                @Override
                public void visitFrame(int type, int numLocal, Object[] local,
                                       int numStack, Object[] stack) {
                    replaceAccessorTypesInFrame(numLocal, local);
                    replaceAccessorTypesInFrame(numStack, stack);
                    super.visitFrame(type, numLocal, local, numStack, stack);
                }

                private void replaceAccessorTypesInFrame(int count, Object[] items) {
                    if (items == null) return;
                    for (int i = 0; i < count; i++) {
                        if (items[i] instanceof String s) {
                            String target = ALL_ACCESSOR_TARGETS.get(s);
                            if (target != null) {
                                items[i] = target;
                            }
                        }
                    }
                }

                // ── Replace CHECKCAST to accessor/extension interfaces with target class ──
                @Override
                public void visitTypeInsn(int opcode, String type) {
                    if (opcode == Opcodes.CHECKCAST) {
                        if (ENTITY_TYPE_EXT.equals(type) || BLOCK_ENTITY_TYPE_EXT.equals(type)) {
                            return; // Flywheel visualizer extensions — skip entirely (handled by bridge)
                        }
                        // Check both Create accessor and Flywheel accessor/extension maps
                        String target = ALL_ACCESSOR_TARGETS.get(type);
                        if (target != null) {
                            super.visitTypeInsn(Opcodes.CHECKCAST, target);
                            return;
                        }
                    }
                    super.visitTypeInsn(opcode, type);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String mName,
                                            String mDescriptor, boolean isInterface) {
                    // Rewrite accessor types in method descriptors throughout
                    String desc = rewriteAccessorDescriptor(mDescriptor);

                    // ── IEventBus.post redirect ────────────────────────────
                    if (opcode == Opcodes.INVOKEINTERFACE
                            && "net/neoforged/bus/api/IEventBus".equals(owner)
                            && "post".equals(mName)
                            && "(Lnet/minecraftforge/eventbus/api/Event;)Lnet/minecraftforge/eventbus/api/Event;".equals(desc)) {
                        super.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                "org/xiyu/reforged/bridge/EventBusHelper",
                                "postAndReturn",
                                "(Lnet/neoforged/bus/api/IEventBus;Lnet/minecraftforge/eventbus/api/Event;)Lnet/minecraftforge/eventbus/api/Event;",
                                false
                        );
                        return;
                    }

                    // ── Flywheel EntityTypeExtension redirects ─────────────
                    if (opcode == Opcodes.INVOKEINTERFACE && ENTITY_TYPE_EXT.equals(owner)) {
                        if ("flywheel$setVisualizer".equals(mName)) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, BRIDGE,
                                    "setEntityVisualizer",
                                    "(Ljava/lang/Object;Ljava/lang/Object;)V", false);
                            return;
                        }
                        if ("flywheel$getVisualizer".equals(mName)) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, BRIDGE,
                                    "getEntityVisualizer",
                                    "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                            super.visitTypeInsn(Opcodes.CHECKCAST,
                                    "dev/engine_room/flywheel/api/visualization/EntityVisualizer");
                            return;
                        }
                    }

                    // ── Flywheel BlockEntityTypeExtension redirects ────────
                    if (opcode == Opcodes.INVOKEINTERFACE && BLOCK_ENTITY_TYPE_EXT.equals(owner)) {
                        if ("flywheel$setVisualizer".equals(mName)) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, BRIDGE,
                                    "setBlockEntityVisualizer",
                                    "(Ljava/lang/Object;Ljava/lang/Object;)V", false);
                            return;
                        }
                        if ("flywheel$getVisualizer".equals(mName)) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, BRIDGE,
                                    "getBlockEntityVisualizer",
                                    "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                            super.visitTypeInsn(Opcodes.CHECKCAST,
                                    "dev/engine_room/flywheel/api/visualization/BlockEntityVisualizer");
                            return;
                        }
                    }

                    // ── Create accessor interface redirects (generic) ──────
                    // Rewrite INVOKEINTERFACE on any Create mixin accessor to
                    // INVOKEVIRTUAL on the actual Minecraft target class.
                    if (opcode == Opcodes.INVOKEINTERFACE
                            && owner.startsWith(CREATE_ACCESSOR_PREFIX)) {
                        String target = CREATE_ACCESSOR_TARGETS.get(owner);
                        if (target != null) {
                            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                    target, mName, desc, false);
                            return;
                        }
                    }

                    // ── Flywheel accessor/extension interface redirects ────
                    // Same pattern: INVOKEINTERFACE → INVOKEVIRTUAL on the MC class.
                    if (opcode == Opcodes.INVOKEINTERFACE) {
                        String target = FLYWHEEL_ACCESSOR_TARGETS.get(owner);
                        if (target != null) {
                            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                    target, mName, desc, false);
                            return;
                        }
                    }

                    super.visitMethodInsn(opcode, owner, mName, desc, isInterface);
                }

                // ── Rewrite accessor types in INVOKEDYNAMIC (lambdas) ──────
                @Override
                public void visitInvokeDynamicInsn(String name, String descriptor,
                                                    Handle bsmHandle, Object... bsmArgs) {
                    String newDesc = rewriteAccessorDescriptor(descriptor);
                    Object[] newArgs = rewriteBootstrapArgs(bsmArgs);
                    super.visitInvokeDynamicInsn(name, newDesc, bsmHandle, newArgs);
                }

                // ── Rewrite accessor types in field access descriptors ─────
                @Override
                public void visitFieldInsn(int opcode, String owner, String name,
                                           String descriptor) {
                    super.visitFieldInsn(opcode, owner, name, rewriteAccessorDescriptor(descriptor));
                }

                // ── Rewrite accessor types in local variable debug info ────
                @Override
                public void visitLocalVariable(String name, String descriptor,
                                               String signature, Label start,
                                               Label end, int index) {
                    super.visitLocalVariable(name, rewriteAccessorDescriptor(descriptor),
                                             signature, start, end, index);
                }
            };
        }
    }
}
