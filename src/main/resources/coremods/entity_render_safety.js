/**
 * Wraps EntityRenderDispatcher.render() in a try-catch to prevent
 * NeoForge mod rendering crashes from killing the game.
 *
 * Create/Flywheel's rendering code may NPE because Flywheel's interface-injection
 * Mixins aren't applied (PoseStackExtension, EntityTypeExtension, etc.).
 * This coremod catches any Throwable during entity rendering and silently
 * skips that entity's render for the frame.
 */
var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var TryCatchBlockNode = Java.type('org.objectweb.asm.tree.TryCatchBlockNode');

function initializeCoreMod() {
    return {
        'entity_render_safety': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.entity.EntityRenderDispatcher',
                'methodName': 'render',
                'methodDesc': '(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V'
            },
            'transformer': function(methodNode) {
                var tryStart = new LabelNode();
                var tryEnd = new LabelNode();
                var catchHandler = new LabelNode();

                // Register the try-catch block: [tryStart, tryEnd) -> catchHandler
                methodNode.tryCatchBlocks.add(new TryCatchBlockNode(
                    tryStart, tryEnd, catchHandler, 'java/lang/Throwable'
                ));

                // Insert tryStart label before the first instruction
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), tryStart);

                // Append tryEnd + catch handler AFTER the entire method body.
                // All existing RETURN instructions remain inside [tryStart, tryEnd)
                // and exit normally. Only thrown exceptions jump to catchHandler.
                methodNode.instructions.add(tryEnd);
                methodNode.instructions.add(catchHandler);
                // The Throwable is on the stack; discard it and return void
                methodNode.instructions.add(new InsnNode(Opcodes.POP));
                methodNode.instructions.add(new InsnNode(Opcodes.RETURN));

                ASMAPI.log('INFO', '[ReForged] Wrapped EntityRenderDispatcher.render() in try-catch safety net');
                return methodNode;
            }
        }
    };
}
