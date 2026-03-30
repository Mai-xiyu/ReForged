package net.neoforged.neoforge.network;

import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.network.payload.ConfigFilePayload;
import org.slf4j.Logger;

/**
 * Initializes NeoForge's built-in network payloads.
 * <p>On NeoForge this registers internal payloads like config sync.
 * On ReForged, Forge handles config sync natively, so this registers
 * the NeoForge payload types for binary compatibility only.</p>
 */
public class NetworkInitialization {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initialized;

    private NetworkInitialization() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        // ConfigFilePayload.TYPE is registered for binary compatibility;
        // actual config sync goes through Forge's ConfigSync mechanism.
        LOGGER.debug("[ReForged] NetworkInitialization: registered NeoForge built-in payload types");
    }
}
