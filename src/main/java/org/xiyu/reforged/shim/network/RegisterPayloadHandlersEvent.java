package org.xiyu.reforged.shim.network;

/**
 * RegisterPayloadHandlersEvent shim — NeoForge fires this event to let mods
 * register their network payloads.
 *
 * <p>In ReForged, we fire this event during mod initialization so that NeoForge
 * mods can register their payloads through our {@link PayloadRegistrar}.</p>
 */
public class RegisterPayloadHandlersEvent extends net.minecraftforge.eventbus.api.Event {

    private final PayloadRegistrar registrar;

    public RegisterPayloadHandlersEvent(String version) {
        this.registrar = new PayloadRegistrar(version);
    }

    /**
     * Get the registrar for registering payloads.
     * NeoForge: {@code event.registrar("1")}
     */
    public PayloadRegistrar registrar(String version) {
        return new PayloadRegistrar(version);
    }

    /**
     * Get the default registrar.
     */
    public PayloadRegistrar registrar() {
        return registrar;
    }
}
