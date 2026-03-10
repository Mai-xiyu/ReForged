package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Fired when a player's arm is rendered in first person. Cancellable.
 */
public class RenderArmEvent extends net.neoforged.bus.api.Event implements net.neoforged.bus.api.ICancellableEvent {
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;
    private final AbstractClientPlayer player;
    private final HumanoidArm arm;

    public RenderArmEvent(PoseStack poseStack, MultiBufferSource multiBufferSource,
            int packedLight, AbstractClientPlayer player, HumanoidArm arm) {
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
        this.player = player;
        this.arm = arm;
    }

    /** Wrapper constructor for EventBusAdapter bridging. */
    public RenderArmEvent(net.minecraftforge.client.event.RenderArmEvent forge) {
        this(forge.getPoseStack(), forge.getMultiBufferSource(), forge.getPackedLight(), forge.getPlayer(), forge.getArm());
    }

    public PoseStack getPoseStack() { return poseStack; }
    public MultiBufferSource getMultiBufferSource() { return multiBufferSource; }
    public int getPackedLight() { return packedLight; }
    public AbstractClientPlayer getPlayer() { return player; }
    public HumanoidArm getArm() { return arm; }
}
