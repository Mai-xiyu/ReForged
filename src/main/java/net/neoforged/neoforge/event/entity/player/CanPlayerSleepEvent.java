package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a player attempts to sleep in a bed.
 */
public class CanPlayerSleepEvent extends PlayerEvent {
    private final BlockPos pos;
    @Nullable private BedSleepingProblem problem;

    public CanPlayerSleepEvent(Player player, BlockPos pos, @Nullable BedSleepingProblem problem) {
        super(player);
        this.pos = pos;
        this.problem = problem;
    }

    public BlockPos getPos() { return pos; }
    @Nullable public BedSleepingProblem getProblem() { return problem; }
    public void setProblem(@Nullable BedSleepingProblem problem) { this.problem = problem; }
}
