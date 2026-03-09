package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.TriState;
import net.minecraftforge.eventbus.api.Cancelable;

/** Wrapper around Forge's {@link net.minecraftforge.event.entity.player.PlayerInteractEvent}. */
public abstract class PlayerInteractEvent extends PlayerEvent {
    private final net.minecraftforge.event.entity.player.PlayerInteractEvent delegate;

    public PlayerInteractEvent() {
        super();
        this.delegate = null;
    }

    protected PlayerInteractEvent(net.minecraftforge.event.entity.player.PlayerInteractEvent delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public Player getEntity() { return delegate.getEntity(); }
    public InteractionHand getHand() { return delegate.getHand(); }
    public ItemStack getItemStack() { return delegate.getItemStack(); }
    public BlockPos getPos() { return delegate.getPos(); }
    public Direction getFace() { return delegate.getFace(); }
    public Level getLevel() { return getEntity().level(); }
    public LogicalSide getSide() { return getLevel().isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER; }
    public InteractionResult getCancellationResult() { return delegate.getCancellationResult(); }
    public void setCancellationResult(InteractionResult result) { delegate.setCancellationResult(result); }

    private static TriState fromForgeResult(net.minecraftforge.eventbus.api.Event.Result result) {
        return switch (result) {
            case ALLOW -> TriState.TRUE;
            case DENY -> TriState.FALSE;
            default -> TriState.DEFAULT;
        };
    }

    private static net.minecraftforge.eventbus.api.Event.Result toForgeResult(TriState state) {
        return switch (state) {
            case TRUE -> net.minecraftforge.eventbus.api.Event.Result.ALLOW;
            case FALSE -> net.minecraftforge.eventbus.api.Event.Result.DENY;
            default -> net.minecraftforge.eventbus.api.Event.Result.DEFAULT;
        };
    }

    @Cancelable
    public static class EntityInteract extends PlayerInteractEvent implements net.neoforged.bus.api.ICancellableEvent {
        private final net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract forgeInteract;

        public EntityInteract(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract delegate) {
            super(delegate);
            this.forgeInteract = delegate;
        }

        public Entity getTarget() { return forgeInteract.getTarget(); }

        @Override
        public void setCanceled(boolean canceled) {
            super.setCanceled(canceled);
            forgeInteract.setCanceled(canceled);
        }
    }

    @Cancelable
    public static class EntityInteractSpecific extends PlayerInteractEvent implements net.neoforged.bus.api.ICancellableEvent {
        private final net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific forgeEvent;

        public EntityInteractSpecific(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public Vec3 getLocalPos() { return forgeEvent.getLocalPos(); }
        public Entity getTarget() { return forgeEvent.getTarget(); }

        @Override
        public void setCanceled(boolean canceled) {
            super.setCanceled(canceled);
            forgeEvent.setCanceled(canceled);
        }
    }

    @Cancelable
    public static class RightClickBlock extends PlayerInteractEvent implements net.neoforged.bus.api.ICancellableEvent {
        private final net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock forgeEvent;

        public RightClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public TriState getUseBlock() { return fromForgeResult(forgeEvent.getUseBlock()); }
        public TriState getUseItem() { return fromForgeResult(forgeEvent.getUseItem()); }
        public BlockHitResult getHitVec() { return forgeEvent.getHitVec(); }
        public void setUseBlock(TriState triggerBlock) { forgeEvent.setUseBlock(toForgeResult(triggerBlock)); }
        public void setUseItem(TriState triggerItem) { forgeEvent.setUseItem(toForgeResult(triggerItem)); }

        @Override
        public void setCanceled(boolean canceled) {
            super.setCanceled(canceled);
            forgeEvent.setCanceled(canceled);
        }
    }

    @Cancelable
    public static class RightClickItem extends PlayerInteractEvent implements net.neoforged.bus.api.ICancellableEvent {
        private final net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem forgeEvent;

        public RightClickItem(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        @Override
        public void setCanceled(boolean canceled) {
            super.setCanceled(canceled);
            forgeEvent.setCanceled(canceled);
        }
    }

    public static class LeftClickEmpty extends PlayerInteractEvent {
        public LeftClickEmpty(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty delegate) {
            super(delegate);
        }
    }

    @Cancelable
    public static class LeftClickBlock extends PlayerInteractEvent implements net.neoforged.bus.api.ICancellableEvent {
        private final net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock forgeEvent;

        public LeftClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public TriState getUseBlock() { return fromForgeResult(forgeEvent.getUseBlock()); }
        public TriState getUseItem() { return fromForgeResult(forgeEvent.getUseItem()); }
        public Action getAction() { return Action.valueOf(forgeEvent.getAction().name()); }
        public void setUseBlock(TriState triggerBlock) { forgeEvent.setUseBlock(toForgeResult(triggerBlock)); }
        public void setUseItem(TriState triggerItem) { forgeEvent.setUseItem(toForgeResult(triggerItem)); }

        @Override
        public void setCanceled(boolean canceled) {
            super.setCanceled(canceled);
            forgeEvent.setCanceled(canceled);
        }

        public enum Action { START, STOP, ABORT, CLIENT_HOLD }
    }
}
