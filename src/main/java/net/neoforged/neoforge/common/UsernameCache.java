package net.neoforged.neoforge.common;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * Cache for mapping UUIDs to last-known usernames.
 */
public class UsernameCache {
    private static final Map<UUID, String> CACHE = new ConcurrentHashMap<>();

    private UsernameCache() {}

    /**
     * Gets the last known username for the given UUID.
     */
    @Nullable
    public static String getLastKnownUsername(UUID uuid) {
        return CACHE.get(uuid);
    }

    /**
     * Returns true if there is a cached username for the given UUID.
     */
    public static boolean containsUUID(UUID uuid) {
        return CACHE.containsKey(uuid);
    }

    /**
     * Returns a copy of the UUID to username mapping.
     */
    public static Map<UUID, String> getMap() {
        return Map.copyOf(CACHE);
    }

    /**
     * Sets a username for the given UUID. Called internally.
     */
    public static void setUsername(UUID uuid, String username) {
        CACHE.put(uuid, username);
    }
}
