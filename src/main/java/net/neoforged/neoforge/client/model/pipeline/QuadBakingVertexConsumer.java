package net.neoforged.neoforge.client.model.pipeline;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A {@link VertexConsumer} that bakes vertices into {@link BakedQuad} instances.
 * This is the primary tool for procedurally generating quads.
 */
public class QuadBakingVertexConsumer implements VertexConsumer {
    private final Consumer<BakedQuad> quadConsumer;
    private final int[] vertices;
    private int vertexIndex;
    private int tintIndex = -1;
    @Nullable private Direction direction;
    @Nullable private TextureAtlasSprite sprite;
    private boolean shade = true;
    private boolean hasAmbientOcclusion = true;

    public QuadBakingVertexConsumer(Consumer<BakedQuad> quadConsumer) {
        this.quadConsumer = quadConsumer;
        this.vertices = new int[DefaultVertexFormat.BLOCK.getVertexSize() / 4 * 4];
        this.vertexIndex = 0;
    }

    public QuadBakingVertexConsumer() {
        this(q -> {});
    }

    public void setDirection(Direction direction) { this.direction = direction; }
    public void setTintIndex(int tintIndex) { this.tintIndex = tintIndex; }
    public void setSprite(@Nullable TextureAtlasSprite sprite) { this.sprite = sprite; }
    public void setShade(boolean shade) { this.shade = shade; }
    public void setHasAmbientOcclusion(boolean ao) { this.hasAmbientOcclusion = ao; }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        int stride = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
        int offset = vertexIndex * stride;
        if (offset + 2 < vertices.length) {
            vertices[offset] = Float.floatToRawIntBits(x);
            vertices[offset + 1] = Float.floatToRawIntBits(y);
            vertices[offset + 2] = Float.floatToRawIntBits(z);
        }
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        vertexIndex++;
        if (vertexIndex >= 4) {
            bake();
        }
        return this;
    }

    private void bake() {
        Direction dir = direction != null ? direction : Direction.UP;
        BakedQuad quad = new BakedQuad(vertices.clone(), tintIndex, dir, sprite, shade);
        quadConsumer.accept(quad);
        vertexIndex = 0;
    }

    /**
     * Convenience method to collect all baked quads.
     */
    public static List<BakedQuad> collectQuads(Consumer<QuadBakingVertexConsumer> builder) {
        List<BakedQuad> quads = new ArrayList<>();
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer(quads::add);
        builder.accept(consumer);
        return quads;
    }
}
