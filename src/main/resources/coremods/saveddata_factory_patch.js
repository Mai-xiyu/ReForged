/**
 * SavedData$Factory 2-arg constructor patch.
 *
 * NeoForge adds a convenience constructor:
 *   SavedData.Factory(Supplier<T>, BiFunction<CompoundTag, HolderLookup.Provider, T>)
 * that Forge lacks (Forge only has the 3-arg version with DataFixTypes).
 *
 * This coremod injects the 2-arg constructor that delegates to the 3-arg one
 * with null as the DataFixTypes parameter.
 *
 * Target: net.minecraft.world.level.saveddata.SavedData$Factory
 */
var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

function initializeCoreMod() {
    return {
        'saveddata_factory_2arg_ctor': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.saveddata.SavedData$Factory'
            },
            'transformer': function(classNode) {
                // Check if 2-arg constructor already exists
                var methods = classNode.methods;
                var twoArgDesc = '(Ljava/util/function/Supplier;Ljava/util/function/BiFunction;)V';
                for (var i = 0; i < methods.size(); i++) {
                    if (methods.get(i).name === '<init>' && methods.get(i).desc === twoArgDesc) {
                        ASMAPI.log('INFO', '[ReForged] SavedData$Factory already has 2-arg constructor');
                        return classNode;
                    }
                }

                // 3-arg constructor descriptor:
                // (Supplier, BiFunction, DataFixTypes) → void
                var threeArgDesc = '(Ljava/util/function/Supplier;Ljava/util/function/BiFunction;Lnet/minecraft/util/datafix/DataFixTypes;)V';

                // Create 2-arg constructor that delegates to 3-arg with null DataFixTypes
                var ctor = new MethodNode(
                    Opcodes.ACC_PUBLIC,
                    '<init>',
                    twoArgDesc,
                    null,
                    null
                );
                ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));   // this
                ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));   // supplier
                ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));   // bifunction
                ctor.instructions.add(new InsnNode(Opcodes.ACONST_NULL));   // null (DataFixTypes)
                ctor.instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESPECIAL,
                    classNode.name,
                    '<init>',
                    threeArgDesc,
                    false
                ));
                ctor.instructions.add(new InsnNode(Opcodes.RETURN));
                ctor.maxStack = 4;
                ctor.maxLocals = 3;

                classNode.methods.add(ctor);
                ASMAPI.log('INFO', '[ReForged] Added 2-arg constructor to SavedData$Factory');
                return classNode;
            }
        }
    };
}
