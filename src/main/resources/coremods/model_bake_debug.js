function initializeCoreMod() {
    var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');

    return {
        'model_bake_debug': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.resources.model.ModelBakery'
            },
            'transformer': function(classNode) {
                var methods = classNode.methods;
                for (var i = 0; i < methods.size(); i++) {
                    var method = methods.get(i);
                    // Find the lambda method that contains "Unable to bake model"
                    if (method.name.indexOf('lambda') >= 0 && method.name.indexOf('bakeModels') >= 0) {
                        // Found the lambda method
                        // Look for the try-catch block and add printStackTrace after exception store
                        var tryCatch = method.tryCatchBlocks;
                        if (tryCatch != null && tryCatch.size() > 0) {
                            for (var j = 0; j < tryCatch.size(); j++) {
                                var tc = tryCatch.get(j);
                                if (tc.type === 'java/lang/Exception') {
                                    // Find the handler's first instruction (astore for exception)
                                    var handlerNode = tc.handler;
                                    // The handler starts with the label, then astore N
                                    var astore = handlerNode.getNext();
                                    if (astore != null && astore.getOpcode() === Opcodes.ASTORE) {
                                        var exVar = astore.var;
                                        // Insert after the astore: aload N; invokevirtual Exception.printStackTrace()
                                        var newInsns = new InsnList();
                                        newInsns.add(new VarInsnNode(Opcodes.ALOAD, exVar));
                                        newInsns.add(new MethodInsnNode(
                                            Opcodes.INVOKEVIRTUAL,
                                            'java/lang/Exception',
                                            'printStackTrace',
                                            '()V',
                                            false
                                        ));
                                        method.instructions.insert(astore, newInsns);
                                        ASMAPI.log('INFO', '[ReForged] Added printStackTrace to ModelBakery bake error handler');
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
                return classNode;
            }
        }
    };
}
