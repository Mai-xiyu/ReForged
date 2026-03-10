package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Fired when a player entity is rendered.
 */
public abstract class RenderPlayerEvent extends PlayerEvent {
    private final PlayerRenderer renderer;
    private final float partialTick;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;

    protected RenderPlayerEvent(Player player, PlayerRenderer renderer, float partialTick,
            PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        super(player);
        this.renderer = renderer;
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
    }

    public PlayerRenderer getRenderer() { return renderer; }
    public float getPartialTick() { return partialTick; }
    public PoseStack getPoseStack() { return poseStack; }
    public MultiBufferSource getMultiBufferSource() { return multiBufferSource; }
    public int getPackedLight() { return packedLight; }

    /** Fired before rendering. Cancellable. */
    public static class Pre extends RenderPlayerEvent implements net.neoforged.bus.api.ICancellableEvent {
        public Pre(Player player, PlayerRenderer renderer, float partialTick,
                PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(player, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public Pre(net.minecraftforge.client.event.RenderPlayerEvent.Pre forge) {
            this((Player) forge.getEntity(), forge.getRenderer(), forge.getPartialTick(),
                    forge.getPoseStack(), forge.getMultiBufferSource(), forge.getPackedLight());
        }
    }

    /** Fired after rendering. */
    public static class Post extends RenderPlayerEvent {
        public Post(Player player, PlayerRenderer renderer, float partialTick,
                PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(player, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public Post(net.minecraftforge.client.event.RenderPlayerEvent.Post forge) {
            this((Player) forge.getEntity(), forge.getRenderer(), forge.getPartialTick(),
                    forge.getPoseStack(), forge.getMultiBufferSource(), forge.getPackedLight());
        }
    }
}
