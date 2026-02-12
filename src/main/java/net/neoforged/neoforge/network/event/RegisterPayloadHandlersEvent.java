package net.neoforged.neoforge.network.event;

import net.minecraftforge.eventbus.api.Event;

/** Proxy: NeoForge RegisterPayloadHandlersEvent */
public class RegisterPayloadHandlersEvent extends Event {
    public net.neoforged.neoforge.network.registration.PayloadRegistrar registrar(String version) {
        return new net.neoforged.neoforge.network.registration.PayloadRegistrar(version);
    }
}
