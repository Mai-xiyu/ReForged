package net.neoforged.neoforge.event.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a chunk watch status changes.
 */
public abstract class ChunkWatchEvent extends Event {
    private final ServerLevel level;
    private final ServerPlayer player;
    private final ChunkPos pos;

    public ChunkWatchEvent(ServerPlayer player, ChunkPos pos, ServerLevel level) {
        this.player = player;
        this.pos = pos;
        this.level = level;
    }

    public ServerPlayer getPlayer() { return player; }
    public ChunkPos getPos() { return pos; }
    public ServerLevel getLevel() { return level; }

    public static class Watch extends ChunkWatchEvent {
        private final LevelChunk chunk;

        public Watch(ServerPlayer player, LevelChunk chunk, ServerLevel level) {
            super(player, chunk.getPos(), level);
            this.chunk = chunk;
        }

        /** Forge wrapper constructor for automatic event bridging */
        public Watch(net.minecraftforge.event.level.ChunkWatchEvent.Watch delegate) {
            this(delegate.getPlayer(), delegate.getChunk(), delegate.getLevel());
        }

        public LevelChunk getChunk() { return chunk; }
    }

    public static class Sent extends ChunkWatchEvent {
        private final LevelChunk chunk;

        public Sent(ServerPlayer player, LevelChunk chunk, ServerLevel level) {
            super(player, chunk.getPos(), level);
            this.chunk = chunk;
        }

        public LevelChunk getChunk() { return chunk; }
    }

    public static class UnWatch extends ChunkWatchEvent {
        public UnWatch(ServerPlayer player, ChunkPos pos, ServerLevel level) {
            super(player, pos, level);
        }

        /** Forge wrapper constructor for automatic event bridging */
        public UnWatch(net.minecraftforge.event.level.ChunkWatchEvent.UnWatch delegate) {
            this(delegate.getPlayer(), delegate.getPos(), delegate.getLevel());
        }
    }
}
