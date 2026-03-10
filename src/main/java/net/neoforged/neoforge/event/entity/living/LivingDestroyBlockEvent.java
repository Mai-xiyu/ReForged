package net.neoforged.neoforge.event.entity.living;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired when the ender dragon/wither attempts to destroy a block or a zombie tries to break a door.
 * Cancel to prevent the block from being destroyed.
 */
public class LivingDestroyBlockEvent extends LivingEvent implements ICancellableEvent {
    private final BlockPos pos;
    private final BlockState state;

    public LivingDestroyBlockEvent(LivingEntity entity, BlockPos pos, BlockState state) {
        super(entity);
        this.pos = pos;
        this.state = state;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public LivingDestroyBlockEvent(net.minecraftforge.event.entity.living.LivingDestroyBlockEvent delegate) {
        this(delegate.getEntity(), delegate.getPos(), delegate.getState());
    }

    public BlockState getState() { return state; }
    public BlockPos getPos() { return pos; }
}
