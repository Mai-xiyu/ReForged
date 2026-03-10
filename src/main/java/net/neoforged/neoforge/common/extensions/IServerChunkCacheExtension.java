package net.neoforged.neoforge.common.extensions;

import net.minecraft.server.level.ServerChunkCache;

/**
 * Extension interface for ServerChunkCache.
 */
public interface IServerChunkCacheExtension {

    default ServerChunkCache self() {
        return (ServerChunkCache) this;
    }
}
