package net.neoforged.neoforge.client.event;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

/**
 * Stub for NeoForge's ModelEvent hierarchy.
 * Houses events related to models.
 */
public abstract class ModelEvent extends Event {
	private static final String STANDALONE_VARIANT = "standalone";

    protected ModelEvent() {}

    /**
     * Fired after model registry is set up, before caching in BlockModelShaper.
     */
    public static class ModifyBakingResult extends ModelEvent implements IModBusEvent {
        private final Map<ModelResourceLocation, BakedModel> models;
        private final Function<Material, TextureAtlasSprite> textureGetter;
        private final ModelBakery modelBakery;

        public ModifyBakingResult(Map<ModelResourceLocation, BakedModel> models,
                                  Function<Material, TextureAtlasSprite> textureGetter,
                                  ModelBakery modelBakery) {
            this.models = models;
            this.textureGetter = textureGetter;
            this.modelBakery = modelBakery;
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public ModifyBakingResult(net.minecraftforge.client.event.ModelEvent.ModifyBakingResult forge) {
            this(forge.getModels(), null, forge.getModelBakery());
        }

        public Map<ModelResourceLocation, BakedModel> getModels() { return models; }
        public Function<Material, TextureAtlasSprite> getTextureGetter() { return textureGetter; }
        public ModelBakery getModelBakery() { return modelBakery; }
    }

    /**
     * Fired after model registry is cached.
     */
    public static class BakingCompleted extends ModelEvent implements IModBusEvent {
        private final ModelManager modelManager;
        private final Map<ModelResourceLocation, BakedModel> models;
        private final ModelBakery modelBakery;

        public BakingCompleted(ModelManager modelManager,
                               Map<ModelResourceLocation, BakedModel> models,
                               ModelBakery modelBakery) {
            this.modelManager = modelManager;
            this.models = models;
            this.modelBakery = modelBakery;
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public BakingCompleted(net.minecraftforge.client.event.ModelEvent.BakingCompleted forge) {
            this(forge.getModelManager(), forge.getModels(), forge.getModelBakery());
        }

        public ModelManager getModelManager() { return modelManager; }
        public Map<ModelResourceLocation, BakedModel> getModels() { return models; }
        public ModelBakery getModelBakery() { return modelBakery; }
    }

    /**
     * Register additional models to be loaded.
     */
    public static class RegisterAdditional extends ModelEvent implements IModBusEvent {
        private final Set<ModelResourceLocation> models;

        public RegisterAdditional(Set<ModelResourceLocation> models) {
            this.models = models;
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public RegisterAdditional(net.minecraftforge.client.event.ModelEvent.RegisterAdditional forge) {
            this(new java.util.HashSet<>());
        }

        public void register(ModelResourceLocation model) {
            Preconditions.checkArgument(
                    model.getVariant().equals(STANDALONE_VARIANT),
                    "Side-loaded models must use the '" + STANDALONE_VARIANT + "' variant");
            models.add(model);
        }
    }

    /**
     * Register custom geometry loaders.
     */
    public static class RegisterGeometryLoaders extends ModelEvent implements IModBusEvent {
        private final Map<ResourceLocation, IGeometryLoader<?>> loaders;

        public RegisterGeometryLoaders(Map<ResourceLocation, IGeometryLoader<?>> loaders) {
            this.loaders = loaders;
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public RegisterGeometryLoaders(net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders forge) {
            this(new java.util.HashMap<>());
        }

        public void register(ResourceLocation key, IGeometryLoader<?> loader) {
            Preconditions.checkArgument(!loaders.containsKey(key), "Geometry loader already registered: " + key);
            loaders.put(key, loader);
        }
    }
}
