package net.neoforged.neoforge.event;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Fired on the server whenever one of Vanilla's GameEvents fire.
 * Cancel this event to prevent the GameEvent from being posted to nearby listeners.
 */
public class VanillaGameEvent extends Event implements ICancellableEvent {
    private final Level level;
    private final Holder<GameEvent> vanillaEvent;
    private final Vec3 position;
    private final GameEvent.Context context;

    public VanillaGameEvent(Level level, Holder<GameEvent> vanillaEvent, Vec3 position, GameEvent.Context context) {
        this.level = level;
        this.vanillaEvent = vanillaEvent;
        this.position = position;
        this.context = context;
    }

    public Level getLevel() { return level; }
    @Nullable public Entity getCause() { return context.sourceEntity(); }
    public Holder<GameEvent> getVanillaEvent() { return vanillaEvent; }
    public Vec3 getEventPosition() { return position; }
    public GameEvent.Context getContext() { return context; }
}
