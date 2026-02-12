package net.neoforged.neoforge.network.registration;

/**
 * Proxy: NeoForge's PayloadRegistrar for network packet registration.
 * Stub — actual network bridging needs more work.
 */
public class PayloadRegistrar {
    private final String version;

    public PayloadRegistrar(String version) {
        this.version = version;
    }

    public String getVersion() { return version; }

    // Stub methods — actual implementations would register network payloads
    public <T> PayloadRegistrar playToServer(Object type, Object codec, Object handler) { return this; }
    public <T> PayloadRegistrar playToClient(Object type, Object codec, Object handler) { return this; }
    public <T> PayloadRegistrar playBidirectional(Object type, Object codec, Object handler) { return this; }
    public PayloadRegistrar optional() { return this; }
}
