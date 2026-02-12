package net.neoforged.neoforge.common;

import net.minecraftforge.common.MinecraftForge;
import net.neoforged.bus.api.IEventBus;

/**
 * Proxy for NeoForge's main class.
 * NeoForge mods access {@code NeoForge.EVENT_BUS} — we redirect to Forge's bus.
 */
public final class NeoForge {
    /**
     * The game-wide event bus. NeoForge mods subscribe to game events here.
     * We use a dynamic proxy to implement {@link net.neoforged.bus.api.IEventBus}
     * while delegating all calls to {@link MinecraftForge#EVENT_BUS}.
     */
    public static final IEventBus EVENT_BUS = (IEventBus) java.lang.reflect.Proxy.newProxyInstance(
            NeoForge.class.getClassLoader(),
            new Class[]{IEventBus.class},
            (proxy, method, args) -> method.invoke(MinecraftForge.EVENT_BUS, args)
    );

    private NeoForge() {}
}
