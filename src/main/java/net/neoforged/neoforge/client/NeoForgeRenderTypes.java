package net.neoforged.neoforge.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Lazy;

/**
 * NeoForge render types. Delegates to custom composite render types for
 * multi-layer item rendering, text rendering with configurable filtering, etc.
 */
@SuppressWarnings("deprecation")
public enum NeoForgeRenderTypes {
    ITEM_LAYERED_SOLID(() -> getItemLayeredSolid(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_LAYERED_CUTOUT(() -> getItemLayeredCutout(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_LAYERED_CUTOUT_MIPPED(() -> getItemLayeredCutoutMipped(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_LAYERED_TRANSLUCENT(() -> getItemLayeredTranslucent(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_UNSORTED_TRANSLUCENT(() -> getUnsortedTranslucent(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_UNLIT_TRANSLUCENT(() -> getUnlitTranslucent(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_UNSORTED_UNLIT_TRANSLUCENT(() -> getUnlitTranslucent(TextureAtlas.LOCATION_BLOCKS, false)),
    TRANSLUCENT_ON_PARTICLES_TARGET(() -> getTranslucentParticlesTarget(TextureAtlas.LOCATION_BLOCKS));

    public static boolean enableTextTextureLinearFiltering = false;

    public static RenderType getItemLayeredSolid(ResourceLocation textureLocation) {
        return Internal.LAYERED_ITEM_SOLID.apply(textureLocation);
    }

    public static RenderType getItemLayeredCutout(ResourceLocation textureLocation) {
        return Internal.LAYERED_ITEM_CUTOUT.apply(textureLocation);
    }

    public static RenderType getItemLayeredCutoutMipped(ResourceLocation textureLocation) {
        return Internal.LAYERED_ITEM_CUTOUT_MIPPED.apply(textureLocation);
    }

    public static RenderType getItemLayeredTranslucent(ResourceLocation textureLocation) {
        return Internal.LAYERED_ITEM_TRANSLUCENT.apply(textureLocation);
    }

    public static RenderType getUnsortedTranslucent(ResourceLocation textureLocation) {
        return Internal.UNSORTED_TRANSLUCENT.apply(textureLocation);
    }

    public static RenderType getUnlitTranslucent(ResourceLocation textureLocation) {
        return Internal.UNLIT_TRANSLUCENT_SORTED.apply(textureLocation);
    }

    public static RenderType getUnlitTranslucent(ResourceLocation textureLocation, boolean sortingEnabled) {
        return (sortingEnabled ? Internal.UNLIT_TRANSLUCENT_SORTED : Internal.UNLIT_TRANSLUCENT_UNSORTED).apply(textureLocation);
    }

    public static RenderType getEntityCutoutMipped(ResourceLocation textureLocation) {
        return Internal.LAYERED_ITEM_CUTOUT_MIPPED.apply(textureLocation);
    }

    public static RenderType getText(ResourceLocation locationIn) {
        return RenderType.text(locationIn);
    }

    public static RenderType getTextIntensity(ResourceLocation locationIn) {
        return RenderType.textIntensity(locationIn);
    }

    public static RenderType getTextPolygonOffset(ResourceLocation locationIn) {
        return RenderType.textPolygonOffset(locationIn);
    }

    public static RenderType getTextIntensityPolygonOffset(ResourceLocation locationIn) {
        return RenderType.textIntensityPolygonOffset(locationIn);
    }

    public static RenderType getTextSeeThrough(ResourceLocation locationIn) {
        return RenderType.textSeeThrough(locationIn);
    }

    public static RenderType getTextIntensitySeeThrough(ResourceLocation locationIn) {
        return RenderType.textIntensitySeeThrough(locationIn);
    }

    public static RenderType getTranslucentParticlesTarget(ResourceLocation locationIn) {
        return Internal.TRANSLUCENT_PARTICLES_TARGET.apply(locationIn);
    }

    private final Supplier<RenderType> renderTypeSupplier;

    NeoForgeRenderTypes(Supplier<RenderType> renderTypeSupplier) {
        this.renderTypeSupplier = Lazy.of(renderTypeSupplier);
    }

    public RenderType get() {
        return renderTypeSupplier.get();
    }

    private static class Internal extends RenderType {
        private Internal(String name, VertexFormat fmt, VertexFormat.Mode glMode, int size,
                         boolean doCrumbling, boolean depthSorting, Runnable onEnable, Runnable onDisable) {
            super(name, fmt, glMode, size, doCrumbling, depthSorting, onEnable, onDisable);
            throw new IllegalStateException("This class must not be instantiated");
        }

        public static Function<ResourceLocation, RenderType> UNSORTED_TRANSLUCENT = Util.memoize(Internal::unsortedTranslucent);

        private static RenderType unsortedTranslucent(ResourceLocation textureLocation) {
            CompositeState renderState = CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(textureLocation, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true);
            return create("neoforge_entity_unsorted_translucent", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 256, true, false, renderState);
        }

        public static Function<ResourceLocation, RenderType> UNLIT_TRANSLUCENT_SORTED = Util.memoize(tex -> unlitTranslucent(tex, true));
        public static Function<ResourceLocation, RenderType> UNLIT_TRANSLUCENT_UNSORTED = Util.memoize(tex -> unlitTranslucent(tex, false));

        private static RenderType unlitTranslucent(ResourceLocation textureLocation, boolean sortingEnabled) {
            // Use entity_translucent shader as fallback (unlit shader requires custom shader registration)
            CompositeState renderState = CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(textureLocation, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true);
            return create("neoforge_entity_unlit_translucent", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 256, true, sortingEnabled, renderState);
        }

        public static Function<ResourceLocation, RenderType> LAYERED_ITEM_SOLID = Util.memoize(Internal::layeredItemSolid);

        private static RenderType layeredItemSolid(ResourceLocation locationIn) {
            CompositeState state = CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
                    .setTextureState(new TextureStateShard(locationIn, false, false))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true);
            return create("neoforge_item_entity_solid", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 256, true, false, state);
        }

        public static Function<ResourceLocation, RenderType> LAYERED_ITEM_CUTOUT = Util.memoize(Internal::layeredItemCutout);

        private static RenderType layeredItemCutout(ResourceLocation locationIn) {
            CompositeState state = CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER)
                    .setTextureState(new TextureStateShard(locationIn, false, false))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true);
            return create("neoforge_item_entity_cutout", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 256, true, false, state);
        }

        public static Function<ResourceLocation, RenderType> LAYERED_ITEM_CUTOUT_MIPPED = Util.memoize(Internal::layeredItemCutoutMipped);

        private static RenderType layeredItemCutoutMipped(ResourceLocation locationIn) {
            CompositeState state = CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_SMOOTH_CUTOUT_SHADER)
                    .setTextureState(new TextureStateShard(locationIn, false, true))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true);
            return create("neoforge_item_entity_cutout_mipped", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 256, true, false, state);
        }

        public static Function<ResourceLocation, RenderType> LAYERED_ITEM_TRANSLUCENT = Util.memoize(Internal::layeredItemTranslucent);

        private static RenderType layeredItemTranslucent(ResourceLocation locationIn) {
            CompositeState state = CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(locationIn, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true);
            return create("neoforge_item_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS, 256, true, true, state);
        }

        public static Function<ResourceLocation, RenderType> TRANSLUCENT_PARTICLES_TARGET = Util.memoize(Internal::translucentParticlesTarget);

        private static RenderType translucentParticlesTarget(ResourceLocation locationIn) {
            CompositeState state = CompositeState.builder()
                    .setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(locationIn, false, true))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOutputState(PARTICLES_TARGET)
                    .createCompositeState(true);
            return create("neoforge_translucent_particles_target", DefaultVertexFormat.BLOCK,
                    VertexFormat.Mode.QUADS, 2097152, true, true, state);
        }
    }
}
