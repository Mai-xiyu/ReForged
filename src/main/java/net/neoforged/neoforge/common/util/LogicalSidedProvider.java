package net.neoforged.neoforge.common.util;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.LogicalSide;

/**
 * Stub: Provides access to logical side-specific objects.
 * Delegates to Forge's ServerLifecycleHooks for server-side access.
 */
public class LogicalSidedProvider {

    @SuppressWarnings("unchecked")
    public static <T> T get(LogicalSide side) {
        if (side == LogicalSide.SERVER) {
            MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            return (T) server;
        }
        // Client side: return Minecraft instance
        try {
            return (T) net.minecraft.client.Minecraft.getInstance();
        } catch (Throwable t) {
            return null;
        }
    }
}
