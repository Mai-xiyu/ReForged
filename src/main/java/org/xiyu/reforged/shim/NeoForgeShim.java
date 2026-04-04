package org.xiyu.reforged.shim;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import org.xiyu.reforged.bridge.NeoForgeEventBusAdapter;
import org.slf4j.Logger;

/**
 * NeoForgeShim — A drop-in replacement for {@code net.neoforged.neoforge.common.NeoForge}.
 *
 * <p>After bytecode rewriting, any NeoForge mod that referenced
 * {@code NeoForge.EVENT_BUS} will instead reference {@code NeoForgeShim.EVENT_BUS}.
 *
 * <p>This class exposes static fields and methods that mirror the NeoForge API shape,
 * but delegate to Forge's native systems under the hood.
 *
 * <h3>NeoForge original API (simplified):</h3>
 * <pre>
 * public class NeoForge {
 *     public static final IEventBus EVENT_BUS = ...;
 * }
 * </pre>
 *
 * <h3>Our shim provides:</h3>
 * <pre>
 * public class NeoForgeShim {
 *     public static final IEventBus EVENT_BUS = ...;
 *     public static final NeoForgeEventBusShim EVENT_BUS_SHIM = ...;
 * }
 * </pre>
 */
public final class NeoForgeShim {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Public EVENT_BUS field for rewritten NeoForge mod bytecode.
        * Descriptor must match NeoForge IEventBus for rewritten NeoForge references.
     */
    public static final IEventBus EVENT_BUS = NeoForgeEventBusAdapter.wrap(MinecraftForge.EVENT_BUS);

    /**
     * Internal bridge bus used by ReForged's own injections that rely on shim-only helpers.
     */
    public static final NeoForgeEventBusShim EVENT_BUS_SHIM = new NeoForgeEventBusShim();

    /**
     * Version string exposed by NeoForge — we return our own identifier.
     */
    public static String getVersion() {
        return "ReForged-Shim-1.0.0";
    }

    /**
     * Called to initialize the NeoForge shim subsystem.
     * Should be invoked during ReForged mod construction.
     */
    public static void init() {
        LOGGER.info("[ReForged] NeoForgeShim initialized — EVENT_BUS is bridged to Forge");
        LOGGER.info("[ReForged] Forge EVENT_BUS type: {}", MinecraftForge.EVENT_BUS.getClass().getName());
    }

    private NeoForgeShim() {
        // Utility class — no instantiation
    }
}
