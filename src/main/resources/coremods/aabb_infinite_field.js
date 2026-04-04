/**
 * AABB.INFINITE field patch.
 *
 * NeoForge adds a static field:
 *   public static final AABB INFINITE
 * which Forge lacks. This coremod adds the field so NeoForge mods
 * (e.g. Create's SchematicannonBlockEntity.getRenderBoundingBox())
 * can reference it without NoSuchFieldError.
 *
 * Value: new AABB(-1e10, -1e10, -1e10, 1e10, 1e10, 1e10)
 *
 * Target: net.minecraft.world.phys.AABB
 */
var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');

function initializeCoreMod() {
    return {
        'aabb_infinite_field': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.phys.AABB'
            },
            'transformer': function(classNode) {
                // Check if INFINITE field already exists
                var fields = classNode.fields;
                for (var i = 0; i < fields.size(); i++) {
                    if (fields.get(i).name === 'INFINITE') {
                        ASMAPI.log('INFO', '[ReForged] AABB.INFINITE already exists, skipping');
                        return classNode;
                    }
                }

                // Add: public static final AABB INFINITE;
                var fieldNode = new FieldNode(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                    'INFINITE',
                    'Lnet/minecraft/world/phys/AABB;',
                    null, null
                );
                classNode.fields.add(fieldNode);

                // Find or create <clinit>
                var clinit = null;
                var methods = classNode.methods;
                for (var i = 0; i < methods.size(); i++) {
                    if (methods.get(i).name === '<clinit>') {
                        clinit = methods.get(i);
                        break;
                    }
                }

                if (clinit === null) {
                    clinit = new MethodNode(Opcodes.ACC_STATIC, '<clinit>', '()V', null, null);
                    clinit.instructions.add(new InsnNode(Opcodes.RETURN));
                    classNode.methods.add(clinit);
                }

                // Build initialization bytecode:
                //   INFINITE = new AABB(-1.0E10, -1.0E10, -1.0E10, 1.0E10, 1.0E10, 1.0E10);
                var init = new InsnList();
                init.add(new TypeInsnNode(Opcodes.NEW, 'net/minecraft/world/phys/AABB'));
                init.add(new InsnNode(Opcodes.DUP));
                // 6 double args: minX, minY, minZ, maxX, maxY, maxZ
                // Nashorn auto-boxes JS numbers to java.lang.Double for LdcInsnNode
                init.add(new LdcInsnNode(-1.0E10));
                init.add(new LdcInsnNode(-1.0E10));
                init.add(new LdcInsnNode(-1.0E10));
                init.add(new LdcInsnNode(1.0E10));
                init.add(new LdcInsnNode(1.0E10));
                init.add(new LdcInsnNode(1.0E10));
                init.add(new MethodInsnNode(
                    Opcodes.INVOKESPECIAL,
                    'net/minecraft/world/phys/AABB',
                    '<init>',
                    '(DDDDDD)V',
                    false
                ));
                init.add(new FieldInsnNode(
                    Opcodes.PUTSTATIC,
                    'net/minecraft/world/phys/AABB',
                    'INFINITE',
                    'Lnet/minecraft/world/phys/AABB;'
                ));

                // Insert before the first RETURN in <clinit>
                var instructions = clinit.instructions;
                var target = null;
                for (var i = 0; i < instructions.size(); i++) {
                    var insn = instructions.get(i);
                    if (insn.getOpcode() === Opcodes.RETURN) {
                        target = insn;
                        break;
                    }
                }

                if (target !== null) {
                    instructions.insertBefore(target, init);
                } else {
                    // No RETURN found, just append
                    instructions.add(init);
                    instructions.add(new InsnNode(Opcodes.RETURN));
                }

                ASMAPI.log('INFO', '[ReForged] Added AABB.INFINITE static field');
                return classNode;
            }
        }
    };
}
