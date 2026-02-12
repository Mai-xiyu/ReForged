package org.xiyu.reforged.bridge;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiyu.reforged.shim.NeoForgeShim;
import org.slf4j.Logger;

/**
 * EventBridge — Generic ALL-event forwarding from Forge's event bus to
 * the NeoForge shim event bus.
 *
 * <h3>Design (Phase 3)</h3>
 * <p>Instead of listing individual events, we subscribe to the base {@link Event}
 * class, which receives <b>every event</b> posted to Forge's bus. We then forward
 * each event to {@link NeoForgeShim#EVENT_BUS} where NeoForge mod handlers are registered.</p>
 *
 * <p>After bytecode rewriting, NeoForge mods expect Forge event types (same classes),
 * so no conversion is needed — we forward the original event object.</p>
 *
 * <h3>Re-entrancy Guard</h3>
 * <p>Uses a ThreadLocal guard to prevent infinite forwarding loops if a NeoForge
 * mod handler re-posts to the shim bus (which could trigger another forward).</p>
 */
public final class EventBridge {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initialized = false;

    /**
     * Guard against re-entrant forwarding (e.g., if a NeoForge mod's handler
     * somehow triggers another event post). ThreadLocal for thread safety with
     * Forge's parallel event dispatch.
     */
    private static final ThreadLocal<Boolean> FORWARDING = ThreadLocal.withInitial(() -> false);

    /**
     * Initialize the event bridge. Registers on Forge's EVENT_BUS to capture
     * ALL events and forward them to the NeoForge shim bus.
     * Safe to call multiple times (idempotent).
     */
    public static void init() {
        if (initialized) return;

        MinecraftForge.EVENT_BUS.register(EventBridge.class);
        initialized = true;

        LOGGER.info("[ReForged] EventBridge initialized — forwarding ALL Forge events to NeoForge shim bus");
    }

    /**
     * Catch-all event handler. Receives every event fired on Forge's EVENT_BUS
     * and forwards it to the NeoForge shim event bus.
     *
     * <p>Using {@link EventPriority#HIGHEST} ensures NeoForge mod handlers see
     * the event early enough to cancel or modify it.</p>
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAnyEvent(Event event) {
        if (FORWARDING.get()) return; // prevent re-entrant loop

        try {
            FORWARDING.set(true);
            NeoForgeShim.EVENT_BUS.post(event);
        } finally {
            FORWARDING.set(false);
        }
    }

    private EventBridge() {}
}
