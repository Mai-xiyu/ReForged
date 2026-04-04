package net.neoforged.neoforge.event.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.util.BlockSnapshot;

/**
 * NeoForge BlockEvent with fields and Forge wrapper constructors.
 */
public class BlockEvent extends Event {
    private LevelAccessor level;
    private BlockPos pos;
    private BlockState state;

    /** Required by Forge's EventListenerHelper */
    public BlockEvent() {}

    public BlockEvent(LevelAccessor level, BlockPos pos, BlockState state) {
        this.level = level;
        this.pos = pos;
        this.state = state;
    }

    /** Wrapper constructor */
    public BlockEvent(net.minecraftforge.event.level.BlockEvent forge) {
        this.level = forge.getLevel();
        this.pos = forge.getPos();
        this.state = forge.getState();
    }

    public LevelAccessor getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    public BlockState getState() { return state; }

    public static class BreakEvent extends BlockEvent implements ICancellableEvent {
        private Player player;
        private int exp;

        /** Required by Forge's EventListenerHelper */
        public BreakEvent() { super(); }

        public BreakEvent(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
            super(level, pos, state);
            this.player = player;
            this.exp = 0;
        }

        /** Wrapper constructor */
        public BreakEvent(net.minecraftforge.event.level.BlockEvent.BreakEvent forge) {
            super(forge);
            this.player = forge.getPlayer();
            this.exp = forge.getExpToDrop();
        }

        public Player getPlayer() { return player; }
        public int getExpToDrop() { return exp; }
        public void setExpToDrop(int exp) { this.exp = exp; }
    }

    public static class EntityPlaceEvent extends BlockEvent implements ICancellableEvent {
        private Entity entity;
        private BlockState placedBlock;
        private BlockState placedAgainst;

        /** Required by Forge's EventListenerHelper */
        public EntityPlaceEvent() { super(); }

        /** NeoForge-style constructor from BlockSnapshot */
        public EntityPlaceEvent(BlockSnapshot snapshot, BlockState placedAgainst, Entity entity) {
            super(snapshot.getLevel(), snapshot.getPos(), snapshot.getBlockState());
            this.entity = entity;
            this.placedBlock = snapshot.getBlockState();
            this.placedAgainst = placedAgainst;
        }

        public EntityPlaceEvent(LevelAccessor level, BlockPos pos, BlockState state,
                                Entity entity, BlockState placedBlock, BlockState placedAgainst) {
            super(level, pos, state);
            this.entity = entity;
            this.placedBlock = placedBlock;
            this.placedAgainst = placedAgainst;
        }

        /** Wrapper constructor */
        public EntityPlaceEvent(net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent forge) {
            super(forge);
            this.entity = forge.getEntity();
            this.placedBlock = forge.getPlacedBlock();
            this.placedAgainst = forge.getPlacedAgainst();
        }

        public Entity getEntity() { return entity; }
        public BlockState getPlacedBlock() { return placedBlock; }
        public BlockState getPlacedAgainst() { return placedAgainst; }
    }

    public static class EntityMultiPlaceEvent extends EntityPlaceEvent {
        /** Required by Forge's EventListenerHelper */
        public EntityMultiPlaceEvent() { super(); }

        public EntityMultiPlaceEvent(LevelAccessor level, BlockPos pos, BlockState state,
                                     Entity entity, BlockState placedBlock, BlockState placedAgainst) {
            super(level, pos, state, entity, placedBlock, placedAgainst);
        }

        /** Wrapper constructor */
        public EntityMultiPlaceEvent(net.minecraftforge.event.level.BlockEvent.EntityMultiPlaceEvent forge) {
            super(forge);
        }
    }
}
