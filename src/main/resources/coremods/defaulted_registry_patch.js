/*
 * Forge JS coremod: adds containsValue(Object) as a default method
 * to net.minecraft.core.DefaultedRegistry interface.
 *
 * NeoForge adds this method; Create calls it via invokeinterface.
 * The real implementation lives in MappedRegistryMixin; this default
 * method satisfies the JVM interface method resolution requirement.
 */
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');

function initializeCoreMod() {
    return {
        'defaulted_registry_containsvalue': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.core.DefaultedRegistry'
            },
            'transformer': function(classNode) {
                // Check if method already exists
                var methods = classNode.methods;
                for (var i = 0; i < methods.size(); i++) {
                    var m = methods.get(i);
                    if (m.name === 'containsValue' && m.desc === '(Ljava/lang/Object;)Z') {
                        return classNode; // already present
                    }
                }

                // Add: default boolean containsValue(Object value)
                // Implementation: throw UnsupportedOperationException
                // (MappedRegistry's mixin provides the real impl that overrides this)
                var mn = new MethodNode(
                    Opcodes.ACC_PUBLIC,
                    'containsValue',
                    '(Ljava/lang/Object;)Z',
                    null,
                    null
                );

                var start = new LabelNode();
                var end = new LabelNode();

                mn.instructions.add(start);
                // Simply return false as default; MappedRegistry overrides this
                mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
                mn.instructions.add(new InsnNode(Opcodes.IRETURN));
                mn.instructions.add(end);

                mn.maxStack = 1;
                mn.maxLocals = 2;

                classNode.methods.add(mn);

                return classNode;
            }
        }
    };
}
