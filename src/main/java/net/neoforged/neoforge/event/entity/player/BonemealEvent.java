package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.Nullable;

@Cancelable
public class BonemealEvent extends PlayerEvent implements ICancellableEvent {
    @Nullable
    private final net.minecraftforge.event.entity.player.BonemealEvent forgeDelegate;
    private final Level level;
    private final BlockPos pos;
    private final BlockState state;
    private final ItemStack stack;
    private final boolean validBonemealTarget;
    private boolean successful;

    public BonemealEvent() {
        super();
        this.forgeDelegate = null;
        this.level = null;
        this.pos = null;
        this.state = null;
        this.stack = ItemStack.EMPTY;
        this.validBonemealTarget = false;
    }

    public BonemealEvent(@Nullable Player player, Level level, BlockPos pos, BlockState state, ItemStack stack) {
        super(player);
        this.forgeDelegate = null;
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.stack = stack;
        this.validBonemealTarget = state.getBlock() instanceof BonemealableBlock bonemealable
                && bonemealable.isValidBonemealTarget(level, pos, state);
    }

    public BonemealEvent(net.minecraftforge.event.entity.player.BonemealEvent forge) {
        super(forge);
        this.forgeDelegate = forge;
        this.level = forge.getLevel();
        this.pos = forge.getPos();
        this.state = forge.getBlock();
        this.stack = forge.getStack();
        this.validBonemealTarget = this.state.getBlock() instanceof BonemealableBlock bonemealable
                && bonemealable.isValidBonemealTarget(this.level, this.pos, this.state);
        this.successful = forge.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW;
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean isValidBonemealTarget() {
        return validBonemealTarget;
    }

    public void setSuccessful(boolean success) {
        this.successful = success;
        if (forgeDelegate != null) {
            forgeDelegate.setResult(success
                    ? net.minecraftforge.eventbus.api.Event.Result.ALLOW
                    : net.minecraftforge.eventbus.api.Event.Result.DEFAULT);
        }
        this.setCanceled(true);
    }

    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        if (forgeDelegate != null) {
            forgeDelegate.setCanceled(canceled);
        }
    }
}
