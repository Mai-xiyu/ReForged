package net.createmod.ponder.mixin.client.accessor;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

import java.lang.reflect.Constructor;

/**
 * Replacement for Ponder's @Accessor mixin on RenderType.
 * Since the original Mixin isn't applied in Forge, we use cached reflection
 * to construct RenderType.CompositeRenderType instances.
 */
public interface RenderTypeAccessor {

    Constructor<?> COMPOSITE_CTOR = findConstructor();

    static Constructor<?> findConstructor() {
        try {
            Class<?> compositeClass = Class.forName(
                    "net.minecraft.client.renderer.RenderType$CompositeRenderType");
            Class<?> compositeState = Class.forName(
                    "net.minecraft.client.renderer.RenderType$CompositeState");
            Constructor<?> ctor = compositeClass.getDeclaredConstructor(
                    String.class, VertexFormat.class, VertexFormat.Mode.class,
                    int.class, boolean.class, boolean.class, compositeState);
            ctor.setAccessible(true);
            return ctor;
        } catch (Exception e) {
            throw new RuntimeException("[ReForged] Failed to find RenderType.CompositeRenderType constructor", e);
        }
    }

    static RenderType.CompositeRenderType catnip$create(
            String name, VertexFormat format, VertexFormat.Mode mode,
            int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
            RenderType.CompositeState state) {
        try {
            return (RenderType.CompositeRenderType) COMPOSITE_CTOR.newInstance(
                    name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, state);
        } catch (Exception e) {
            throw new RuntimeException("[ReForged] Failed to create RenderType.CompositeRenderType", e);
        }
    }
}
