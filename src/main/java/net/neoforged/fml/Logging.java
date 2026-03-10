package net.neoforged.fml;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * NeoForge log markers for consistent logging.
 */
public class Logging {
    public static final Marker FORGEMOD = MarkerManager.getMarker("FORGEMOD");
    public static final Marker LOADING = MarkerManager.getMarker("LOADING");
    public static final Marker CORE = MarkerManager.getMarker("CORE");
    public static final Marker CAPABILITIES = MarkerManager.getMarker("CAPABILITIES");
    public static final Marker SPLASH = MarkerManager.getMarker("SPLASH");

    private Logging() {}
}
