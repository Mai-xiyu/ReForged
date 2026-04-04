package net.neoforged.neoforge.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Captures a snapshot of a block at a given position, for later restore.
 */
public class BlockSnapshot {
    private final Level level;
    private final BlockPos pos;
    private final BlockState state;
    private final BlockState replacedState;
    @Nullable
    private final CompoundTag nbt;
    private final int flag;

    public BlockSnapshot(Level level, BlockPos pos, BlockState state) {
        this(level, pos, state, state, getTileNBT(level, pos), 3);
    }

    public BlockSnapshot(Level level, BlockPos pos, BlockState state, BlockState replacedState,
                         @Nullable CompoundTag nbt, int flag) {
        this.level = level;
        this.pos = pos.immutable();
        this.state = state;
        this.replacedState = replacedState;
        this.nbt = nbt;
        this.flag = flag;
    }

    public static BlockSnapshot create(Level level, BlockPos pos) {
        return new BlockSnapshot(level, pos, level.getBlockState(pos));
    }

    public static BlockSnapshot create(Level level, BlockPos pos, int flag) {
        return new BlockSnapshot(level, pos, level.getBlockState(pos), level.getBlockState(pos),
                getTileNBT(level, pos), flag);
    }

    public static BlockSnapshot create(ResourceKey<Level> dim, LevelAccessor levelAccessor, BlockPos pos) {
        if (levelAccessor instanceof Level level) {
            return create(level, pos);
        }
        throw new IllegalArgumentException("BlockSnapshot.create requires a Level instance, got " + levelAccessor.getClass().getName());
    }

    public static BlockSnapshot create(ResourceKey<Level> dim, LevelAccessor levelAccessor, BlockPos pos, int flag) {
        if (levelAccessor instanceof Level level) {
            return create(level, pos, flag);
        }
        throw new IllegalArgumentException("BlockSnapshot.create requires a Level instance, got " + levelAccessor.getClass().getName());
    }

    public Level getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    public BlockState getBlockState() { return state; }
    public BlockState getReplacedState() { return replacedState; }
    @Nullable
    public CompoundTag getTag() { return nbt; }
    public int getFlag() { return flag; }

    public Block getBlock() { return state.getBlock(); }

    /**
     * Restores the block to the state captured in this snapshot.
     */
    public boolean restore() {
        return restore(true);
    }

    public boolean restore(boolean force) {
        return restore(force, true);
    }

    public boolean restore(boolean force, boolean applyPhysics) {
        BlockState current = level.getBlockState(pos);
        if (!force && current != replacedState) return false;
        boolean result = level.setBlock(pos, state, applyPhysics ? 3 : 2);
        if (result && nbt != null) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te != null) {
                te.loadWithComponents(nbt, level.registryAccess());
            }
        }
        return result;
    }

    @Nullable
    private static CompoundTag getTileNBT(Level level, BlockPos pos) {
        BlockEntity te = level.getBlockEntity(pos);
        return te != null ? te.saveWithFullMetadata(level.registryAccess()) : null;
    }
}
