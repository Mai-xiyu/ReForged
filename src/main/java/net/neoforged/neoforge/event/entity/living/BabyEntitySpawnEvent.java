package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: Fired when a baby entity is spawned (breeding).
 */
public class BabyEntitySpawnEvent extends LivingEvent {
    private final Mob parentA;
    private final Mob parentB;
    @Nullable
    private final Player causedByPlayer;
    @Nullable
    private AgeableMob child;

    public BabyEntitySpawnEvent(Mob parentA, Mob parentB, @Nullable AgeableMob child, @Nullable Player causedByPlayer) {
        super(parentA);
        this.parentA = parentA;
        this.parentB = parentB;
        this.child = child;
        this.causedByPlayer = causedByPlayer;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public BabyEntitySpawnEvent(net.minecraftforge.event.entity.living.BabyEntitySpawnEvent delegate) {
        this(delegate.getParentA(), delegate.getParentB(), delegate.getChild(), delegate.getCausedByPlayer());
    }

    public Mob getParentA() { return parentA; }
    public Mob getParentB() { return parentB; }
    @Nullable public AgeableMob getChild() { return child; }
    public void setChild(@Nullable AgeableMob child) { this.child = child; }
    @Nullable public Player getCausedByPlayer() { return causedByPlayer; }
}
