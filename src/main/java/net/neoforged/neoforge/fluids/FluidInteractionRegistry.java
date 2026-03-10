package net.neoforged.neoforge.fluids;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Registry for fluid interaction behaviors (e.g., lava + water = obsidian).
 */
public final class FluidInteractionRegistry {
    private static final Map<FluidType, List<InteractionInformation>> INTERACTIONS = new ConcurrentHashMap<>();

    private FluidInteractionRegistry() {}

    /**
     * Registers a fluid interaction for the given source fluid type.
     */
    public static synchronized void addInteraction(FluidType source, InteractionInformation interaction) {
        INTERACTIONS.computeIfAbsent(source, k -> new ArrayList<>()).add(interaction);
    }

    /**
     * Checks if any fluid interaction should occur at the given position.
     */
    public static boolean canInteract(Level level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (fluidState.isEmpty()) return false;
        FluidType type = (FluidType) (Object) fluidState.getFluidType();
        List<InteractionInformation> interactions = INTERACTIONS.get(type);
        if (interactions == null) return false;
        for (InteractionInformation info : interactions) {
            for (Direction dir : Direction.values()) {
                if (info.predicate.test(new FluidInteraction(level, pos, dir))) {
                    info.interaction.interact(level, pos, dir);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Data class for fluid interaction context.
     */
    public record FluidInteraction(Level level, BlockPos pos, Direction direction) {
    }

    /**
     * Functional interface for performing a fluid interaction.
     */
    @FunctionalInterface
    public interface HasFluidInteraction {
        void interact(Level level, BlockPos pos, Direction direction);
    }

    /**
     * Holds the predicate and action for a fluid interaction.
     */
    public record InteractionInformation(Predicate<FluidInteraction> predicate, HasFluidInteraction interaction) {
    }
}
