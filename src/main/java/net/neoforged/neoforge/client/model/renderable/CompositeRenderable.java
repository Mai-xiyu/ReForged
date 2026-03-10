package net.neoforged.neoforge.client.model.renderable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.HashMap;
import java.util.Map;

/**
 * A composite renderable composed of named parts.
 */
public class CompositeRenderable implements IRenderable<CompositeRenderable.Transforms> {
    private final Map<String, IRenderable<?>> parts;

    private CompositeRenderable(Map<String, IRenderable<?>> parts) {
        this.parts = parts;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick, Transforms context) {
        for (var entry : parts.entrySet()) {
            Transformation transform = context.get(entry.getKey());
            poseStack.pushPose();
            if (!transform.isIdentity()) {
                poseStack.last().pose().mul(transform.getMatrix());
            }
            ((IRenderable) entry.getValue()).render(poseStack, bufferSource, packedLight, packedOverlay, partialTick, null);
            poseStack.popPose();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, IRenderable<?>> parts = new HashMap<>();

        public Builder addPart(String name, IRenderable<?> renderable) {
            parts.put(name, renderable);
            return this;
        }

        public CompositeRenderable build() {
            return new CompositeRenderable(new HashMap<>(parts));
        }
    }

    /**
     * Transform context for composite renderables.
     */
    public static class Transforms {
        public static final Transforms EMPTY = new Transforms(Map.of());
        private final Map<String, Transformation> transforms;

        public Transforms(Map<String, Transformation> transforms) {
            this.transforms = transforms;
        }

        public Transformation get(String part) {
            return transforms.getOrDefault(part, Transformation.identity());
        }
    }
}
