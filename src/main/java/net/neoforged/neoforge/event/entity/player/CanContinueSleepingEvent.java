package net.neoforged.neoforge.event.entity.player;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Fired to check if a sleeping entity can continue sleeping.
 */
public class CanContinueSleepingEvent extends LivingEvent {
    @Nullable protected final BedSleepingProblem problem;
    protected boolean mayContinueSleeping;

    public CanContinueSleepingEvent(LivingEntity entity, @Nullable BedSleepingProblem problem) {
        super(entity);
        this.problem = problem;
        this.mayContinueSleeping = (problem == null);
    }

    Optional<BlockPos> getSleepingPos() { return getEntity().getSleepingPos(); }
    @Nullable public BedSleepingProblem getProblem() { return problem; }
    public boolean mayContinueSleeping() { return mayContinueSleeping; }
    public void setContinueSleeping(boolean sleeping) { this.mayContinueSleeping = sleeping; }
}
