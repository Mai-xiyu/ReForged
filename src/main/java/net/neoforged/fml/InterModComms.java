package net.neoforged.fml;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Proxy: NeoForge's InterModComms → delegates to Forge's InterModComms.
 */
public final class InterModComms {
    private InterModComms() {}

    public static boolean sendTo(String modId, String method, Supplier<?> thing) {
        return net.minecraftforge.fml.InterModComms.sendTo(modId, method, thing);
    }

    /**
     * NeoForge IMCMessage record — used by mods that consume IMC messages.
     */
    public record IMCMessage(String senderModId, String modId, String method, Supplier<?> messageSupplier) {

        /**
         * Get the message payload.
         */
        @SuppressWarnings("unchecked")
        public <T> T getMessageSupplier() {
            return (T) messageSupplier.get();
        }
    }

    /**
     * Retrieve all messages for a given mod and method.
     */
    public static Stream<IMCMessage> getMessages(String modId, java.util.function.Predicate<String> methodFilter) {
        return net.minecraftforge.fml.InterModComms.getMessages(modId, methodFilter)
                .map(msg -> new IMCMessage(msg.senderModId(), modId, msg.method(), msg.messageSupplier()));
    }

    /**
     * Retrieve all messages for a given mod.
     */
    public static Stream<IMCMessage> getMessages(String modId) {
        return getMessages(modId, s -> true);
    }
}
