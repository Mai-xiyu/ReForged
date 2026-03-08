package net.neoforged.neoforge.server;

import net.minecraft.server.MinecraftServer;

/**
 * NeoForge ServerLifecycleHooks shim — delegates to Forge's ServerLifecycleHooks.
 */
public class ServerLifecycleHooks {

    public static MinecraftServer getCurrentServer() {
        return net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
    }
}
