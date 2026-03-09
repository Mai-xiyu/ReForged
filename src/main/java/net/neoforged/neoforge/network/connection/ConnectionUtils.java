package net.neoforged.neoforge.network.connection;

import net.minecraft.network.Connection;

/**
 * Stub: Connection utility methods.
 */
public class ConnectionUtils {
    private ConnectionUtils() {}

    public static ConnectionType getConnectionType(Connection connection) {
        return connection != null ? ConnectionType.NEOFORGE : ConnectionType.OTHER;
    }
}
