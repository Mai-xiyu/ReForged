package net.neoforged.neoforge.client.model.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * A {@link VertexConsumer} wrapper that delegates all operations to a parent consumer.
 * Useful as a base class for vertex consumer decorators.
 */
public class VertexConsumerWrapper implements VertexConsumer {
    protected final VertexConsumer parent;

    public VertexConsumerWrapper(VertexConsumer parent) {
        this.parent = parent;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        parent.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        parent.setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        parent.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        parent.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        parent.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        parent.setNormal(x, y, z);
        return this;
    }
}
