package net.neoforged.neoforge.event.level;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Fired when a chunk-related event occurs.
 */
public abstract class ChunkEvent extends LevelEvent {
    private final ChunkAccess chunk;

    public ChunkEvent(ChunkAccess chunk, LevelAccessor level) {
        super(level);
        this.chunk = chunk;
    }

    public ChunkAccess getChunk() { return chunk; }

    public static class Load extends ChunkEvent {
        private final boolean newChunk;

        public Load(ChunkAccess chunk, LevelAccessor level, boolean newChunk) {
            super(chunk, level);
            this.newChunk = newChunk;
        }

        /** Forge wrapper constructor for automatic event bridging */
        public Load(net.minecraftforge.event.level.ChunkEvent.Load delegate) {
            this(delegate.getChunk(), delegate.getLevel(), delegate.isNewChunk());
        }

        public boolean isNewChunk() { return newChunk; }
    }

    public static class Unload extends ChunkEvent {
        public Unload(ChunkAccess chunk, LevelAccessor level) {
            super(chunk, level);
        }

        /** Forge wrapper constructor for automatic event bridging */
        public Unload(net.minecraftforge.event.level.ChunkEvent.Unload delegate) {
            this(delegate.getChunk(), delegate.getLevel());
        }
    }
}
