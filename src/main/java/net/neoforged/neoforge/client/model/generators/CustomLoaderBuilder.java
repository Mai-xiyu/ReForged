package net.neoforged.neoforge.client.model.generators;

import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Abstract base for custom model loader builders used in data generation.
 */
public abstract class CustomLoaderBuilder<T extends ModelBuilder<T>> {
    protected final ResourceLocation loaderId;
    protected final T parent;
    protected final ExistingFileHelper existingFileHelper;
    protected final Map<String, Boolean> visibility = new LinkedHashMap<>();
    protected boolean allowInlineElements = false;
    private boolean optional = false;

    protected CustomLoaderBuilder(ResourceLocation loaderId, T parent, ExistingFileHelper existingFileHelper,
                                  boolean allowInlineElements) {
        this.loaderId = loaderId;
        this.parent = parent;
        this.existingFileHelper = existingFileHelper;
        this.allowInlineElements = allowInlineElements;
    }

    protected CustomLoaderBuilder(ResourceLocation loaderId, T parent, ExistingFileHelper existingFileHelper) {
        this(loaderId, parent, existingFileHelper, false);
    }

    public CustomLoaderBuilder<T> visibility(String partName, boolean show) {
        this.visibility.put(partName, show);
        return this;
    }

    public CustomLoaderBuilder<T> optional() {
        this.optional = true;
        return this;
    }

    public T end() {
        return parent;
    }

    public JsonObject toJson(JsonObject json) {
        json.addProperty("loader", loaderId.toString());
        if (optional) {
            json.addProperty("neoforge:optional_loader", true);
        }
        if (!visibility.isEmpty()) {
            JsonObject vis = new JsonObject();
            visibility.forEach(vis::addProperty);
            json.add("visibility", vis);
        }
        return json;
    }
}
