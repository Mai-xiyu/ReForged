package net.neoforged.neoforge.client.model.generators.loaders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Builder for item layer models with support for emissive layers, per-layer colors and render types.
 */
public class ItemLayerModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> ItemLayerModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new ItemLayerModelBuilder<>(parent, existingFileHelper);
    }

    private final Int2ObjectMap<ExtraFaceData> faceData = new Int2ObjectOpenHashMap<>();
    private final Map<ResourceLocation, IntSet> renderTypes = new LinkedHashMap<>();
    private final IntSet layersWithRenderTypes = new IntOpenHashSet();

    protected ItemLayerModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(ResourceLocation.fromNamespaceAndPath("neoforge", "item_layers"), parent, existingFileHelper, false);
    }

    /**
     * Marks a set of layers to be rendered emissively.
     *
     * @param blockLight The block light (0-15)
     * @param skyLight   The sky light (0-15)
     * @param layers     the layers that will render unlit
     * @return this builder
     */
    public ItemLayerModelBuilder<T> emissive(int blockLight, int skyLight, int... layers) {
        Preconditions.checkNotNull(layers, "Layers must not be null");
        Preconditions.checkArgument(layers.length > 0, "At least one layer must be specified");
        Preconditions.checkArgument(Arrays.stream(layers).allMatch(i -> i >= 0), "All layers must be >= 0");
        for (int i : layers) {
            faceData.compute(i, (key, value) -> {
                ExtraFaceData fallback = value == null ? ExtraFaceData.DEFAULT : value;
                return new ExtraFaceData(fallback.color(), blockLight, skyLight, fallback.ambientOcclusion());
            });
        }
        return this;
    }

    /**
     * Marks a set of layers to be rendered with a specific color.
     *
     * @param color  The color, in ARGB.
     * @param layers the layers that will render with color
     * @return this builder
     */
    public ItemLayerModelBuilder<T> color(int color, int... layers) {
        Preconditions.checkNotNull(layers, "Layers must not be null");
        Preconditions.checkArgument(layers.length > 0, "At least one layer must be specified");
        Preconditions.checkArgument(Arrays.stream(layers).allMatch(i -> i >= 0), "All layers must be >= 0");
        for (int i : layers) {
            faceData.compute(i, (key, value) -> {
                ExtraFaceData fallback = value == null ? ExtraFaceData.DEFAULT : value;
                return new ExtraFaceData(color, fallback.blockLight(), fallback.skyLight(), fallback.ambientOcclusion());
            });
        }
        return this;
    }

    /**
     * Set the render type for a set of layers.
     */
    public ItemLayerModelBuilder<T> renderType(String renderType, int... layers) {
        Preconditions.checkNotNull(renderType, "Render type must not be null");
        ResourceLocation asLoc;
        if (renderType.contains(":"))
            asLoc = ResourceLocation.parse(renderType);
        else
            asLoc = ResourceLocation.fromNamespaceAndPath(parent.getLocation().getNamespace(), renderType);
        return renderType(asLoc, layers);
    }

    /**
     * Set the render type for a set of layers.
     */
    public ItemLayerModelBuilder<T> renderType(ResourceLocation renderType, int... layers) {
        Preconditions.checkNotNull(renderType, "Render type must not be null");
        Preconditions.checkNotNull(layers, "Layers must not be null");
        Preconditions.checkArgument(layers.length > 0, "At least one layer must be specified");
        Preconditions.checkArgument(Arrays.stream(layers).allMatch(i -> i >= 0), "All layers must be >= 0");
        var alreadyAssigned = Arrays.stream(layers).filter(layersWithRenderTypes::contains).toArray();
        Preconditions.checkArgument(alreadyAssigned.length == 0, "Attempted to re-assign layer render types: " + Arrays.toString(alreadyAssigned));
        var renderTypeLayers = renderTypes.computeIfAbsent(renderType, $ -> new IntOpenHashSet());
        Arrays.stream(layers).forEach(layer -> {
            renderTypeLayers.add(layer);
            layersWithRenderTypes.add(layer);
        });
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);

        JsonObject forgeData = new JsonObject();
        JsonObject layerObj = new JsonObject();

        for (Int2ObjectMap.Entry<ExtraFaceData> entry : this.faceData.int2ObjectEntrySet()) {
            layerObj.add(String.valueOf(entry.getIntKey()), ExtraFaceData.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow());
        }

        forgeData.add("layers", layerObj);
        json.add("neoforge_data", forgeData);

        JsonObject renderTypes = new JsonObject();
        this.renderTypes.forEach((renderType, typeLayers) -> {
            JsonArray array = new JsonArray();
            typeLayers.intStream().sorted().forEach(array::add);
            renderTypes.add(renderType.toString(), array);
        });
        json.add("render_types", renderTypes);

        return json;
    }
}
