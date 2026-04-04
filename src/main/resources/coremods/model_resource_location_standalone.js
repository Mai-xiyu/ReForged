/**
 * ModelResourceLocation.standalone() patch.
 *
 * NeoForge adds a static method:
 *   ModelResourceLocation.standalone(ResourceLocation) -> ModelResourceLocation
 * which Forge lacks. This coremod adds the method so NeoForge mods can register
 * partial models / additional models with the "standalone" variant.
 *
 * Target: net.minecraft.client.resources.model.ModelResourceLocation
 */
var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');

function initializeCoreMod() {
    return {
        'model_resource_location_standalone': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.resources.model.ModelResourceLocation'
            },
            'transformer': function(classNode) {
                // Check if standalone() already exists
                var methods = classNode.methods;
                var standaloneDesc = '(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/resources/model/ModelResourceLocation;';

                for (var i = 0; i < methods.size(); i++) {
                    if (methods.get(i).name === 'standalone') {
                        ASMAPI.log('INFO', '[ReForged] ModelResourceLocation.standalone() already exists, skipping');
                        return classNode;
                    }
                }

                // Add: public static ModelResourceLocation standalone(ResourceLocation loc) {
                //     return new ModelResourceLocation(loc, "standalone");
                // }
                var mn = new MethodNode(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    'standalone',
                    standaloneDesc,
                    null, null
                );

                var il = mn.instructions;
                // NEW ModelResourceLocation
                il.add(new TypeInsnNode(Opcodes.NEW,
                    'net/minecraft/client/resources/model/ModelResourceLocation'));
                // DUP
                il.add(new InsnNode(Opcodes.DUP));
                // ALOAD 0 (ResourceLocation argument)
                il.add(new VarInsnNode(Opcodes.ALOAD, 0));
                // LDC "standalone"
                il.add(new LdcInsnNode('standalone'));
                // INVOKESPECIAL <init>(ResourceLocation, String)
                il.add(new MethodInsnNode(
                    Opcodes.INVOKESPECIAL,
                    'net/minecraft/client/resources/model/ModelResourceLocation',
                    '<init>',
                    '(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;)V',
                    false
                ));
                // ARETURN
                il.add(new InsnNode(Opcodes.ARETURN));

                mn.maxStack = 4;
                mn.maxLocals = 1;

                classNode.methods.add(mn);
                ASMAPI.log('INFO', '[ReForged] Added ModelResourceLocation.standalone(ResourceLocation) method');
                return classNode;
            }
        }
    };
}
