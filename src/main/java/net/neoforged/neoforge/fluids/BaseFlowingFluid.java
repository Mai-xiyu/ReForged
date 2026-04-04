package net.neoforged.neoforge.fluids;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;

/**
 * Stub: NeoForge's BaseFlowingFluid — base class for mod flowing fluids.
 */
public abstract class BaseFlowingFluid extends FlowingFluid {

    private final Supplier<? extends FluidType> fluidType;
    private final Supplier<? extends Fluid> flowing;
    private final Supplier<? extends Fluid> still;
    private final Supplier<? extends Item> bucket;
    private final Supplier<? extends LiquidBlock> block;
    private final int slopeFindDistance;
    private final int levelDecreasePerBlock;
    private final float explosionResistance;
    private final int tickRate;

    protected BaseFlowingFluid(Properties properties) {
        this.fluidType = properties.fluidType;
        this.flowing = properties.flowing;
        this.still = properties.still;
        this.bucket = properties.bucket;
        this.block = properties.block;
        this.slopeFindDistance = properties.slopeFindDistance;
        this.levelDecreasePerBlock = properties.levelDecreasePerBlock;
        this.explosionResistance = properties.explosionResistance;
        this.tickRate = properties.tickRate;
    }

    @Override
    public Fluid getFlowing() { return flowing.get(); }

    @Override
    public Fluid getSource() { return still.get(); }

    @Override
    public Item getBucket() { return bucket != null ? bucket.get() : Items.AIR; }

    @Override
    protected float getExplosionResistance() { return explosionResistance; }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
        return Vec3.ZERO;
    }

    @Override
    protected boolean canConvertToSource(Level level) { return false; }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {}

    @Override
    protected int getSlopeFindDistance(LevelReader level) { return slopeFindDistance; }

    @Override
    protected int getDropOff(LevelReader level) { return levelDecreasePerBlock; }

    @Override
    public int getTickDelay(LevelReader level) { return tickRate; }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        super.createFluidStateDefinition(builder);
        builder.add(LEVEL);
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        if (block != null) {
            return block.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        }
        return Blocks.AIR.defaultBlockState();
    }

    public FluidType getFluidType() { return fluidType.get(); }

    public static class Flowing extends BaseFlowingFluid {
        public Flowing(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(net.minecraft.world.level.material.FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(net.minecraft.world.level.material.FluidState state) {
            return false;
        }
    }

    public static class Source extends BaseFlowingFluid {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(net.minecraft.world.level.material.FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(net.minecraft.world.level.material.FluidState state) {
            return true;
        }
    }

    public static class Properties {
        Supplier<? extends FluidType> fluidType;
        Supplier<? extends Fluid> still;
        Supplier<? extends Fluid> flowing;
        Supplier<? extends Item> bucket;
        Supplier<? extends LiquidBlock> block;
        int slopeFindDistance = 4;
        int levelDecreasePerBlock = 1;
        float explosionResistance = 1;
        int tickRate = 5;

        public Properties(Supplier<? extends FluidType> fluidType, Supplier<? extends Fluid> still,
                          Supplier<? extends Fluid> flowing) {
            this.fluidType = fluidType;
            this.still = still;
            this.flowing = flowing;
        }

        public Properties bucket(Supplier<? extends Item> bucket) {
            this.bucket = bucket;
            return this;
        }

        public Properties block(Supplier<? extends LiquidBlock> block) {
            this.block = block;
            return this;
        }

        public Properties slopeFindDistance(int slopeFindDistance) {
            this.slopeFindDistance = slopeFindDistance;
            return this;
        }

        public Properties levelDecreasePerBlock(int levelDecreasePerBlock) {
            this.levelDecreasePerBlock = levelDecreasePerBlock;
            return this;
        }

        public Properties explosionResistance(float explosionResistance) {
            this.explosionResistance = explosionResistance;
            return this;
        }

        public Properties tickRate(int tickRate) {
            this.tickRate = tickRate;
            return this;
        }
    }
}
