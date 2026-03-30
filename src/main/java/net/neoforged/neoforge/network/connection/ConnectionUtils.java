package net.neoforged.neoforge.network.connection;

import net.minecraft.network.Connection;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/**
 * Connection utility methods for determining connection type.
 * <p>On ReForged, connections to servers are always treated as NEOFORGE type
 * when NeoForge mods are loaded, since the payload bridge is active.</p>
 */
public class ConnectionUtils {
    private ConnectionUtils() {}

    /**
     * Determine the connection type for a given network connection.
     *
     * @param connection the network connection to check
     * @return the connection type
     */
    public static ConnectionType getConnectionType(Connection connection) {
        if (connection == null) return ConnectionType.OTHER;
        // On ReForged, we treat all connections as NEOFORGE since the payload
        // bridge translates NeoForge payloads to Forge SimpleChannel packets.
        return ConnectionType.NEOFORGE;
    }

    /**
     * Check if a connection supports NeoForge payloads.
     */
    public static boolean isNeoForge(Connection connection) {
        return getConnectionType(connection) == ConnectionType.NEOFORGE;
    }
}
