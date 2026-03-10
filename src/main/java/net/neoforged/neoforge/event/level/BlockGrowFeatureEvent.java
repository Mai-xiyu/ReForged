package net.neoforged.neoforge.event.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

/**
 * Fired when a sapling grows into a tree (or similar block grow feature).
 */
public class BlockGrowFeatureEvent extends Event {
    private final LevelAccessor level;
    private final BlockPos pos;
    @Nullable
    private Holder<ConfiguredFeature<?, ?>> feature;

    public BlockGrowFeatureEvent(LevelAccessor level, BlockPos pos, @Nullable Holder<ConfiguredFeature<?, ?>> feature) {
        this.level = level;
        this.pos = pos;
        this.feature = feature;
    }

    /** Wrapper from Forge SaplingGrowTreeEvent */
    public BlockGrowFeatureEvent(net.minecraftforge.event.level.SaplingGrowTreeEvent forge) {
        this.level = forge.getLevel();
        this.pos = forge.getPos();
        this.feature = forge.getFeature();
    }

    public LevelAccessor getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    @Nullable
    public Holder<ConfiguredFeature<?, ?>> getFeature() { return feature; }
    public void setFeature(@Nullable Holder<ConfiguredFeature<?, ?>> feature) { this.feature = feature; }
}
