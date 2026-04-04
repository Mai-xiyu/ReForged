/**
 * IEventBus post() bridge coremod.
 *
 * After bytecode rewriting, NeoForge mod calls to IEventBus.post(Event)
 * have the descriptor:
 *   (Lnet/minecraftforge/eventbus/api/Event;)Lnet/minecraftforge/eventbus/api/Event;
 *
 * But our IEventBus only has:
 *   boolean post(net.minecraftforge.eventbus.api.Event)  — inherited from Forge
 *   net.neoforged.bus.api.Event post(net.neoforged.bus.api.Event)  — our default method
 *
 * Neither matches the rewritten descriptor. This coremod adds an ABSTRACT method
 * with the exact descriptor so the Proxy creates a dispatch for it, routing
 * through InvocationHandler where we handle post() explicitly.
 *
 * Target: net.neoforged.bus.api.IEventBus
 */
var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');

function initializeCoreMod() {
    return {
        'ieventbus_post_bridge': {
            'target': {
                'type': 'CLASS',
                'name': 'net.neoforged.bus.api.IEventBus'
            },
            'transformer': function(classNode) {
                var bridgeDesc = '(Lnet/minecraftforge/eventbus/api/Event;)Lnet/minecraftforge/eventbus/api/Event;';

                // Check if method already exists
                var methods = classNode.methods;
                for (var i = 0; i < methods.size(); i++) {
                    if (methods.get(i).name === 'post' && methods.get(i).desc === bridgeDesc) {
                        ASMAPI.log('INFO', '[ReForged] IEventBus already has post bridge method');
                        return classNode;
                    }
                }

                // Add abstract interface method — Proxy routes it to InvocationHandler
                var method = new MethodNode(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
                    'post',
                    bridgeDesc,
                    null,
                    null
                );

                classNode.methods.add(method);
                ASMAPI.log('INFO', '[ReForged] Added abstract post() bridge method to IEventBus');
                return classNode;
            }
        }
    };
}
