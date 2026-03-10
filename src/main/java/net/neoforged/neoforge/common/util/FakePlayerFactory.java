package net.neoforged.neoforge.common.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and caching FakePlayer instances.
 */
public class FakePlayerFactory {
    private static final Map<GameProfile, FakePlayer> FAKE_PLAYERS = new ConcurrentHashMap<>();
    private static final GameProfile MINECRAFT = new GameProfile(
            UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77"), "[Minecraft]");

    private FakePlayerFactory() {}

    /**
     * Gets a FakePlayer for the Minecraft profile.
     */
    public static FakePlayer getMinecraft(ServerLevel level) {
        return get(level, MINECRAFT);
    }

    /**
     * Gets or creates a FakePlayer for the given profile.
     */
    public static FakePlayer get(ServerLevel level, GameProfile profile) {
        return FAKE_PLAYERS.computeIfAbsent(profile, p -> new FakePlayer(level, p));
    }

    /**
     * Removes all cached fake players. Should be called on server stop.
     */
    public static void unloadLevel(ServerLevel level) {
        FAKE_PLAYERS.values().removeIf(player -> player.level() == level);
    }
}
