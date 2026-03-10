package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Fired when a living entity is rendered.
 */
@SuppressWarnings("unchecked")
public abstract class RenderLivingEvent<T extends LivingEntity, M extends EntityModel<T>>
        extends net.neoforged.bus.api.Event {
    private final LivingEntity entity;
    private final LivingEntityRenderer<T, M> renderer;
    private final float partialTick;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;

    @SuppressWarnings("unchecked")
    protected RenderLivingEvent(LivingEntity entity, LivingEntityRenderer<?, ?> renderer,
            float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        this.entity = entity;
        this.renderer = (LivingEntityRenderer<T, M>) renderer;
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
    }

    public LivingEntity getEntity() { return entity; }
    public LivingEntityRenderer<T, M> getRenderer() { return renderer; }
    public float getPartialTick() { return partialTick; }
    public PoseStack getPoseStack() { return poseStack; }
    public MultiBufferSource getMultiBufferSource() { return multiBufferSource; }
    public int getPackedLight() { return packedLight; }

    /** Fired before rendering. Cancellable. */
    public static class Pre<T extends LivingEntity, M extends EntityModel<T>>
            extends RenderLivingEvent<T, M> implements net.neoforged.bus.api.ICancellableEvent {
        public Pre(LivingEntity entity, LivingEntityRenderer<?, ?> renderer,
                float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(entity, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        @SuppressWarnings("rawtypes")
        public Pre(net.minecraftforge.client.event.RenderLivingEvent.Pre forge) {
            this(forge.getEntity(), forge.getRenderer(), forge.getPartialTick(),
                    forge.getPoseStack(), forge.getMultiBufferSource(), forge.getPackedLight());
        }
    }

    /** Fired after rendering. */
    public static class Post<T extends LivingEntity, M extends EntityModel<T>>
            extends RenderLivingEvent<T, M> {
        public Post(LivingEntity entity, LivingEntityRenderer<?, ?> renderer,
                float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(entity, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        @SuppressWarnings("rawtypes")
        public Post(net.minecraftforge.client.event.RenderLivingEvent.Post forge) {
            this(forge.getEntity(), forge.getRenderer(), forge.getPartialTick(),
                    forge.getPoseStack(), forge.getMultiBufferSource(), forge.getPackedLight());
        }
    }
}
