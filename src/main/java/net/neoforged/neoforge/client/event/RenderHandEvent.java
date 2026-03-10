package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stub for NeoForge's RenderHandEvent.
 * Fired when a hand is rendered in first person.
 */
public class RenderHandEvent extends Event implements ICancellableEvent {
    private final InteractionHand hand;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;
    private final float partialTick;
    private final float interpolatedPitch;
    private final float swingProgress;
    private final float equipProgress;
    private final ItemStack stack;

    public RenderHandEvent(InteractionHand hand, PoseStack poseStack, MultiBufferSource multiBufferSource,
                           int packedLight, float partialTick, float interpolatedPitch,
                           float swingProgress, float equipProgress, ItemStack stack) {
        this.hand = hand;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
        this.partialTick = partialTick;
        this.interpolatedPitch = interpolatedPitch;
        this.swingProgress = swingProgress;
        this.equipProgress = equipProgress;
        this.stack = stack;
    }

    /** Wrapper constructor for EventBusAdapter bridging. */
    public RenderHandEvent(net.minecraftforge.client.event.RenderHandEvent forge) {
        this(forge.getHand(), forge.getPoseStack(), forge.getMultiBufferSource(), forge.getPackedLight(),
                forge.getPartialTick(), forge.getInterpolatedPitch(), forge.getSwingProgress(),
                forge.getEquipProgress(), forge.getItemStack());
    }

    public InteractionHand getHand() { return hand; }
    public PoseStack getPoseStack() { return poseStack; }
    public MultiBufferSource getMultiBufferSource() { return multiBufferSource; }
    public int getPackedLight() { return packedLight; }
    public float getPartialTick() { return partialTick; }
    public float getInterpolatedPitch() { return interpolatedPitch; }
    public float getSwingProgress() { return swingProgress; }
    public float getEquipProgress() { return equipProgress; }
    public ItemStack getItemStack() { return stack; }
}
