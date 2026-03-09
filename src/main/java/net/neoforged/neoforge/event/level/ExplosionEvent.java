package net.neoforged.neoforge.event.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.List;

public abstract class ExplosionEvent extends Event {
    private final Level level;
    private final Explosion explosion;

    public ExplosionEvent(Level level, Explosion explosion) {
        this.level = level;
        this.explosion = explosion;
    }

    public ExplosionEvent(net.minecraftforge.event.level.ExplosionEvent delegate) {
        this(delegate.getLevel(), delegate.getExplosion());
    }

    public Level getLevel() {
        return level;
    }

    public Explosion getExplosion() {
        return explosion;
    }

    @Cancelable
    public static class Start extends ExplosionEvent implements ICancellableEvent {
        private final net.minecraftforge.event.level.ExplosionEvent.Start forgeDelegate;

        public Start(Level level, Explosion explosion) {
            super(level, explosion);
            this.forgeDelegate = null;
        }

        public Start(net.minecraftforge.event.level.ExplosionEvent.Start delegate) {
            super(delegate);
            this.forgeDelegate = delegate;
        }

        @Override
        public void setCanceled(boolean canceled) {
            super.setCanceled(canceled);
            if (forgeDelegate != null) {
                forgeDelegate.setCanceled(canceled);
            }
        }
    }

    public static class Detonate extends ExplosionEvent {
        private final List<Entity> entityList;

        public Detonate(Level level, Explosion explosion, List<Entity> entityList) {
            super(level, explosion);
            this.entityList = entityList;
        }

        public Detonate(net.minecraftforge.event.level.ExplosionEvent.Detonate delegate) {
            super(delegate);
            this.entityList = delegate.getAffectedEntities();
        }

        public List<BlockPos> getAffectedBlocks() {
            return getExplosion().getToBlow();
        }

        public List<Entity> getAffectedEntities() {
            return entityList;
        }
    }
}
