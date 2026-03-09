package net.neoforged.neoforge.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CapabilityListenerHolder {
    private final Map<Long, Map<Long, Set<ListenerReference>>> byChunkThenBlock = new HashMap<>();
    private final ReferenceQueue<ICapabilityInvalidationListener> queue = new ReferenceQueue<>();

    public synchronized void addListener(BlockPos pos, ICapabilityInvalidationListener listener) {
        pos = pos.immutable();
        Map<Long, Set<ListenerReference>> chunkHolder = byChunkThenBlock.computeIfAbsent(ChunkPos.asLong(pos), ignored -> new HashMap<>());
        Set<ListenerReference> listeners = chunkHolder.computeIfAbsent(pos.asLong(), ignored -> new HashSet<>());
        ListenerReference reference = new ListenerReference(queue, pos, listener);
        if (!listeners.add(reference)) {
            reference.clear();
        }
    }

    public synchronized void invalidatePos(BlockPos pos) {
        Map<Long, Set<ListenerReference>> chunkHolder = byChunkThenBlock.get(ChunkPos.asLong(pos));
        if (chunkHolder == null) {
            return;
        }
        Set<ListenerReference> listeners = chunkHolder.get(pos.asLong());
        if (listeners != null) {
            invalidateList(listeners);
        }
    }

    public synchronized void invalidateChunk(ChunkPos chunkPos) {
        Map<Long, Set<ListenerReference>> chunkHolder = byChunkThenBlock.get(chunkPos.toLong());
        if (chunkHolder == null) {
            return;
        }
        for (Set<ListenerReference> listeners : chunkHolder.values()) {
            invalidateList(listeners);
        }
    }

    private void invalidateList(Set<ListenerReference> listeners) {
        listeners.removeIf(reference -> {
            ICapabilityInvalidationListener listener = reference.get();
            return listener == null || !listener.onInvalidate();
        });
    }

    public synchronized void clean() {
        while (true) {
            ListenerReference reference = (ListenerReference) queue.poll();
            if (reference == null) {
                return;
            }

            Map<Long, Set<ListenerReference>> chunkHolder = byChunkThenBlock.get(ChunkPos.asLong(reference.pos));
            if (chunkHolder == null) {
                continue;
            }

            Set<ListenerReference> listeners = chunkHolder.get(reference.pos.asLong());
            if (listeners == null) {
                continue;
            }

            boolean removed = listeners.remove(reference);
            if (removed && listeners.isEmpty()) {
                chunkHolder.remove(reference.pos.asLong());
                if (chunkHolder.isEmpty()) {
                    byChunkThenBlock.remove(ChunkPos.asLong(reference.pos));
                }
            }
        }
    }

    private static final class ListenerReference extends WeakReference<ICapabilityInvalidationListener> {
        private final BlockPos pos;
        private final int listenerHashCode;

        private ListenerReference(ReferenceQueue<ICapabilityInvalidationListener> queue, BlockPos pos, ICapabilityInvalidationListener listener) {
            super(listener, queue);
            this.pos = pos;
            this.listenerHashCode = System.identityHashCode(listener);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ListenerReference otherReference) {
                return otherReference.listenerHashCode == listenerHashCode && otherReference.get() == get();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return listenerHashCode;
        }
    }
}