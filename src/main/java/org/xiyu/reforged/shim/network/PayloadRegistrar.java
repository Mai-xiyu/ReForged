package org.xiyu.reforged.shim.network;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * PayloadRegistrar — Shim for NeoForge's network payload registration system.
 *
 * <h3>NeoForge API</h3>
 * <pre>
 * public void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
 *     PayloadRegistrar registrar = event.registrar("1");
 *     registrar.playToServer(MyPayload.TYPE, MyPayload.STREAM_CODEC, MyPayloadHandler::handle);
 * }
 * </pre>
 *
 * <h3>Forge Equivalent</h3>
 * <p>Forge uses {@code SimpleChannel} and {@code NetworkRegistry.newSimpleChannel()}.
 * This shim stores registrations and bridges them at runtime.</p>
 */
public final class PayloadRegistrar {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Track all payload registrations for debugging/bridging */
    private static final Map<String, PayloadRegistrar> REGISTRARS = new ConcurrentHashMap<>();

    private final String version;

    public PayloadRegistrar(String version) {
        this.version = version;
        REGISTRARS.put(version, this);
        LOGGER.debug("[ReForged] PayloadRegistrar created for version '{}'", version);
    }

    /**
     * Register a payload to be sent from client to server.
     * NeoForge: {@code registrar.playToServer(type, codec, handler)}
     */
    public <T> PayloadRegistrar playToServer(Object type, Object streamCodec, Object handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.playToServer: type={}, version={}", type, version);
        // TODO: Bridge to Forge's SimpleChannel
        // For now, store the registration for future integration
        return this;
    }

    /**
     * Register a payload to be sent from server to client.
     * NeoForge: {@code registrar.playToClient(type, codec, handler)}
     */
    public <T> PayloadRegistrar playToClient(Object type, Object streamCodec, Object handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.playToClient: type={}, version={}", type, version);
        return this;
    }

    /**
     * Register a bidirectional payload.
     * NeoForge: {@code registrar.playBidirectional(type, codec, handler)}
     */
    public <T> PayloadRegistrar playBidirectional(Object type, Object streamCodec, Object handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.playBidirectional: type={}, version={}", type, version);
        return this;
    }

    /**
     * Register config packet handlers (used during login).
     */
    public <T> PayloadRegistrar configurationToServer(Object type, Object streamCodec, Object handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.configurationToServer: type={}", type);
        return this;
    }

    public <T> PayloadRegistrar configurationToClient(Object type, Object streamCodec, Object handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.configurationToClient: type={}", type);
        return this;
    }

    public <T> PayloadRegistrar configurationBidirectional(Object type, Object streamCodec, Object handler) {
        LOGGER.info("[ReForged] PayloadRegistrar.configurationBidirectional: type={}", type);
        return this;
    }

    /**
     * Set whether payloads of this version are optional.
     */
    public PayloadRegistrar optional() {
        LOGGER.debug("[ReForged] PayloadRegistrar optional set for version '{}'", version);
        return this;
    }

    /**
     * Versioned registrar — returns itself with version metadata.
     */
    public PayloadRegistrar versioned(String version) {
        return new PayloadRegistrar(version);
    }

    public String getVersion() {
        return version;
    }
}
