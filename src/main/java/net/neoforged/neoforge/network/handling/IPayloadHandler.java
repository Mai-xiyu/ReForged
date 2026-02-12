package net.neoforged.neoforge.network.handling;

/** Proxy: NeoForge's IPayloadHandler */
@FunctionalInterface
public interface IPayloadHandler<T> {
    void handle(T payload, IPayloadContext context);
}
