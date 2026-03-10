package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fired when a block screen effect is rendered (e.g. fire overlay, water overlay). Cancellable.
 */
public class RenderBlockScreenEffectEvent extends net.neoforged.bus.api.Event
        implements net.neoforged.bus.api.ICancellableEvent {
    private final Player player;
    private final PoseStack poseStack;
    private final OverlayType overlayType;
    private final BlockState blockState;
    private final BlockPos blockPos;

    public RenderBlockScreenEffectEvent(Player player, PoseStack poseStack, OverlayType overlayType,
            BlockState blockState, BlockPos blockPos) {
        this.player = player;
        this.poseStack = poseStack;
        this.overlayType = overlayType;
        this.blockState = blockState;
        this.blockPos = blockPos;
    }

    public Player getPlayer() { return player; }
    public PoseStack getPoseStack() { return poseStack; }
    public OverlayType getOverlayType() { return overlayType; }
    public BlockState getBlockState() { return blockState; }
    public BlockPos getBlockPos() { return blockPos; }

    public enum OverlayType {
        FIRE,
        WATER,
        BLOCK
    }
}
