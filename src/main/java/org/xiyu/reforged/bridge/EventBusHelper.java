package org.xiyu.reforged.bridge;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.Event;
import org.slf4j.Logger;

/**
 * Static helper methods for bridging NeoForge ↔ Forge event bus API differences.
 *
 * <p>These methods are called by bytecode that has been redirected by
 * {@link org.xiyu.reforged.asm.BytecodeRewriter.MethodCallRedirector}.</p>
 */
public final class EventBusHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private EventBusHelper() {}

    /**
     * Bridge for NeoForge's {@code IEventBus.post(Event)} which returns the Event,
     * vs Forge's {@code IEventBus.post(Event)} which returns boolean.
     *
     * <p>After bytecode rewriting, NeoForge mod calls have descriptor
     * {@code (Lnet/minecraftforge/eventbus/api/Event;)Lnet/minecraftforge/eventbus/api/Event;}
     * which doesn't match either method on the proxy. The BytecodeRewriter redirects
     * these calls here as {@code INVOKESTATIC}.</p>
     *
     * <p>We bypass the NeoForge proxy entirely because Java's Proxy resolves
     * {@code post(Event)} to Forge's {@code boolean post(Event)} (due to parameter
     * type matching), and the proxy auto-unboxing machinery then tries to cast the
     * returned Event to Boolean → ClassCastException.</p>
     *
     * @param bus   the IEventBus (our NeoForge proxy wrapping Forge's bus)
     * @param event the event to post
     * @return the same event instance (NeoForge convention)
     */
    public static Event postAndReturn(net.neoforged.bus.api.IEventBus bus, Event event) {
        // 1. Dispatch to fallback listeners (handles Flywheel / Create custom events)
        NeoForgeEventBusAdapter.dispatchFallback(event);

        // 2. Also try posting to Forge's game bus directly (bypassing the proxy)
        try {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
        } catch (Throwable t) {
            LOGGER.debug("[ReForged] Forge bus post() failed for {}: {}",
                    event.getClass().getSimpleName(), t.getMessage());
        }

        return event;
    }
}
