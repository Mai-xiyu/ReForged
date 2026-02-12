package org.xiyu.reforged.shim;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
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
 *     public static final NeoForgeEventBusShim EVENT_BUS = ...;
 * }
 * </pre>
 */
public final class NeoForgeShim {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * The "NeoForge" event bus — actually our bridge that forwards to Forge's bus.
     *
     * <p>NeoForge mods call {@code NeoForge.EVENT_BUS.register(this)} at startup.
     * After rewriting, that becomes {@code NeoForgeShim.EVENT_BUS.register(this)},
     * which delegates to this shim instance.
     */
    public static final NeoForgeEventBusShim EVENT_BUS = new NeoForgeEventBusShim();

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
