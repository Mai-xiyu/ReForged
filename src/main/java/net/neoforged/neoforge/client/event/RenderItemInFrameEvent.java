package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired before an item stack is rendered in an item frame.
 * If cancelled, the item stack will not be rendered.
 */
public class RenderItemInFrameEvent extends Event implements ICancellableEvent {
    private final ItemStack itemStack;
    private final ItemFrame itemFrameEntity;
    private final ItemFrameRenderer<?> renderer;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;

    public RenderItemInFrameEvent(ItemFrame itemFrame, ItemFrameRenderer<?> renderer, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int packedLight) {
        this.itemStack = itemFrame.getItem();
        this.itemFrameEntity = itemFrame;
        this.renderer = renderer;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
    }

    /** Wrapper constructor for EventBusAdapter bridging. */
    public RenderItemInFrameEvent(net.minecraftforge.client.event.RenderItemInFrameEvent forge) {
        this(forge.getItemFrameEntity(), forge.getRenderer(), forge.getPoseStack(),
                forge.getMultiBufferSource(), forge.getPackedLight());
    }

    public ItemStack getItemStack() { return itemStack; }
    public ItemFrame getItemFrameEntity() { return itemFrameEntity; }
    public ItemFrameRenderer<?> getRenderer() { return renderer; }
    public PoseStack getPoseStack() { return poseStack; }
    public MultiBufferSource getMultiBufferSource() { return multiBufferSource; }
    public int getPackedLight() { return packedLight; }
}
