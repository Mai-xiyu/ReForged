package net.neoforged.neoforge.client.model;

import com.mojang.math.Transformation;
import net.minecraft.Util;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;

/**
 * A collection of {@link IQuadTransformer} implementations.
 */
public final class QuadTransformers {
    private static final IQuadTransformer EMPTY = quad -> {};
    private static final IQuadTransformer[] EMISSIVE_TRANSFORMERS = Util.make(new IQuadTransformer[16], array -> {
        Arrays.setAll(array, i -> applyingLightmap(LightTexture.pack(i, i)));
    });

    public static IQuadTransformer empty() {
        return EMPTY;
    }

    public static IQuadTransformer applying(Transformation transform) {
        if (transform.isIdentity())
            return empty();
        return quad -> {
            var vertices = quad.getVertices();
            for (int i = 0; i < 4; i++) {
                int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
                float x = Float.intBitsToFloat(vertices[offset]);
                float y = Float.intBitsToFloat(vertices[offset + 1]);
                float z = Float.intBitsToFloat(vertices[offset + 2]);
                Vector4f pos = new Vector4f(x, y, z, 1);
                transform.transformPosition(pos);
                pos.div(pos.w());
                vertices[offset] = Float.floatToRawIntBits(pos.x());
                vertices[offset + 1] = Float.floatToRawIntBits(pos.y());
                vertices[offset + 2] = Float.floatToRawIntBits(pos.z());
            }
            for (int i = 0; i < 4; i++) {
                int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.NORMAL;
                int normalIn = vertices[offset];
                if ((normalIn & 0x00FFFFFF) != 0) {
                    float nx = ((byte) (normalIn & 0xFF)) / 127.0f;
                    float ny = ((byte) ((normalIn >> 8) & 0xFF)) / 127.0f;
                    float nz = ((byte) ((normalIn >> 16) & 0xFF)) / 127.0f;
                    Vector3f n = new Vector3f(nx, ny, nz);
                    transform.transformNormal(n);
                    vertices[offset] = (((byte) (n.x() * 127.0f)) & 0xFF) |
                            ((((byte) (n.y() * 127.0f)) & 0xFF) << 8) |
                            ((((byte) (n.z() * 127.0f)) & 0xFF) << 16) |
                            (normalIn & 0xFF000000);
                }
            }
        };
    }

    public static IQuadTransformer applyingLightmap(int packedLight) {
        return quad -> {
            var vertices = quad.getVertices();
            for (int i = 0; i < 4; i++)
                vertices[i * IQuadTransformer.STRIDE + IQuadTransformer.UV2] = packedLight;
        };
    }

    public static IQuadTransformer applyingLightmap(int blockLight, int skyLight) {
        return applyingLightmap(LightTexture.pack(blockLight, skyLight));
    }

    public static IQuadTransformer settingEmissivity(int emissivity) {
        if (emissivity < 0 || emissivity >= 16)
            throw new IllegalArgumentException("Emissivity must be between 0 and 15.");
        return EMISSIVE_TRANSFORMERS[emissivity];
    }

    public static IQuadTransformer settingMaxEmissivity() {
        return EMISSIVE_TRANSFORMERS[15];
    }

    public static IQuadTransformer applyingColor(int color) {
        final int fixedColor = toABGR(color);
        return quad -> {
            var vertices = quad.getVertices();
            for (int i = 0; i < 4; i++)
                vertices[i * IQuadTransformer.STRIDE + IQuadTransformer.COLOR] = fixedColor;
        };
    }

    public static int toABGR(int argb) {
        return (argb & 0xFF00FF00) | ((argb >> 16) & 0x000000FF) | ((argb << 16) & 0x00FF0000);
    }

    private QuadTransformers() {}
}
