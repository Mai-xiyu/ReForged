package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityEvent;

/**
 * Stub: Fired when an entity's name tag is rendered.
 */
public class RenderNameTagEvent extends EntityEvent {
    private final Component originalContent;
    private final EntityRenderer<?> entityRenderer;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;
    private final float partialTick;
    private Component content;
    private TriState canRender = TriState.DEFAULT;

    public RenderNameTagEvent(Entity entity, Component content) {
        this(entity, content, null, null, null, 0, 0f);
    }

	public RenderNameTagEvent(Entity entity, Component content, EntityRenderer<?> entityRenderer, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, float partialTick) {
		super(entity);
		this.originalContent = content;
		this.content = content;
		this.entityRenderer = entityRenderer;
		this.poseStack = poseStack;
		this.multiBufferSource = multiBufferSource;
		this.packedLight = packedLight;
		this.partialTick = partialTick;
    }

    public Component getContent() { return content; }
    public void setContent(Component content) { this.content = content; }
    public Component getOriginalContent() { return originalContent; }
    public void setCanRender(TriState canRender) { this.canRender = canRender; }
    public TriState canRender() { return canRender; }
    public EntityRenderer<?> getEntityRenderer() { return entityRenderer; }
    public PoseStack getPoseStack() { return poseStack; }
    public MultiBufferSource getMultiBufferSource() { return multiBufferSource; }
    public int getPackedLight() { return packedLight; }
    public float getPartialTick() { return partialTick; }
}
