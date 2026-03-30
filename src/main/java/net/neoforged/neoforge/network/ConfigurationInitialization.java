package net.neoforged.neoforge.network;

import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.slf4j.Logger;

/**
 * Handles NeoForge network configuration phase initialization.
 * <p>On NeoForge, this registers built-in configuration payloads (config sync, etc.).
 * On ReForged, we delegate to {@link NetworkRegistry} for payload registration
 * and trigger config sync via Forge's existing mechanisms.</p>
 */
public class ConfigurationInitialization {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initialized = false;

    private ConfigurationInitialization() {}

    /**
     * Initialize the NeoForge network configuration phase.
     * Called during mod loading to set up payload handlers.
     */
    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        try {
            NetworkRegistry.setup();
            LOGGER.debug("[ReForged] ConfigurationInitialization completed");
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] ConfigurationInitialization failed (non-fatal): {}", t.getMessage());
        }
    }
}
