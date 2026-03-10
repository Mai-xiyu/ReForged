package net.neoforged.neoforge.fluids;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Collection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Fluid content information for cauldrons.
 *
 * <p>Empty, water and lava cauldrons are registered by default,
 * and additional cauldrons can be registered with {@link RegisterCauldronFluidContentEvent}.
 */
public final class CauldronFluidContent {
    public final Block block;
    public final Fluid fluid;
    public final int totalAmount;
    public final int maxLevel;
    @Nullable
    public final IntegerProperty levelProperty;

    /**
     * Return the current level of the cauldron given its block state, or 0 if it's an empty cauldron.
     */
    public int currentLevel(BlockState state) {
        if (fluid == Fluids.EMPTY) {
            return 0;
        } else if (levelProperty == null) {
            return 1;
        } else {
            return state.getValue(levelProperty);
        }
    }

    private CauldronFluidContent(Block block, Fluid fluid, int totalAmount, int maxLevel, @Nullable IntegerProperty levelProperty) {
        this.block = block;
        this.fluid = fluid;
        this.totalAmount = totalAmount;
        this.maxLevel = maxLevel;
        this.levelProperty = levelProperty;
    }

    private static final Map<Block, CauldronFluidContent> BLOCK_TO_CAULDRON = new IdentityHashMap<>();
    private static final Map<Fluid, CauldronFluidContent> FLUID_TO_CAULDRON = new IdentityHashMap<>();

    @Nullable
    public static CauldronFluidContent getForBlock(Block block) {
        return BLOCK_TO_CAULDRON.get(block);
    }

    @Nullable
    public static CauldronFluidContent getForFluid(Fluid fluid) {
        return FLUID_TO_CAULDRON.get(fluid);
    }

    @ApiStatus.Internal
    public static void init() {
        var registerEvent = new RegisterCauldronFluidContentEvent();
        registerEvent.register(Blocks.CAULDRON, Fluids.EMPTY, FluidType.BUCKET_VOLUME, null);
        registerEvent.register(Blocks.WATER_CAULDRON, Fluids.WATER, FluidType.BUCKET_VOLUME, LayeredCauldronBlock.LEVEL);
        registerEvent.register(Blocks.LAVA_CAULDRON, Fluids.LAVA, FluidType.BUCKET_VOLUME, null);
    }

    /**
     * Register a cauldron fluid content. Called by RegisterCauldronFluidContentEvent.
     */
    static void register(Block block, Fluid fluid, int totalAmount, @Nullable IntegerProperty levelProperty) {
        if (BLOCK_TO_CAULDRON.get(block) != null) {
            throw new IllegalArgumentException("Duplicate cauldron registration for block %s.".formatted(block));
        }
        if (fluid != Fluids.EMPTY && FLUID_TO_CAULDRON.get(fluid) != null) {
            throw new IllegalArgumentException("Duplicate cauldron registration for fluid %s.".formatted(fluid));
        }
        if (totalAmount <= 0) {
            throw new IllegalArgumentException("Cauldron total amount %d should be positive.".formatted(totalAmount));
        }

        CauldronFluidContent data;

        if (levelProperty == null) {
            data = new CauldronFluidContent(block, fluid, totalAmount, 1, null);
        } else {
            Collection<Integer> levels = levelProperty.getPossibleValues();
            if (levels.isEmpty()) {
                throw new IllegalArgumentException("Cauldron should have at least one possible level.");
            }

            int minLevel = Integer.MAX_VALUE;
            int maxLevel = 0;
            for (int level : levels) {
                minLevel = Math.min(minLevel, level);
                maxLevel = Math.max(maxLevel, level);
            }

            if (minLevel != 1) {
                throw new IllegalStateException("Minimum level should be 1, and maximum level should be >= 1.");
            }

            data = new CauldronFluidContent(block, fluid, totalAmount, maxLevel, levelProperty);
        }

        BLOCK_TO_CAULDRON.put(block, data);
        if (fluid != Fluids.EMPTY) {
            FLUID_TO_CAULDRON.put(fluid, data);
        }
    }
}
