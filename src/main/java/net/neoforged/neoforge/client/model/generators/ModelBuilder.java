package net.neoforged.neoforge.client.model.generators;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shim: NeoForge's ModelBuilder.
 * <p>
 * Must extend NeoForge's {@link ModelFile} (not Forge's ModelBuilder) so that
 * NeoForge mods that pass a {@code ModelBuilder} where a {@code ModelFile} is expected
 * pass the JVM bytecode verifier's type-assignability check.
 *
 * @param <T> Self type, for simpler chaining of methods.
 */
public class ModelBuilder<T extends ModelBuilder<T>> extends ModelFile {

    @Nullable
    protected ModelFile parent;
    protected final Map<String, String> textures = new LinkedHashMap<>();
    protected final ExistingFileHelper existingFileHelper;
    protected String renderType = null;
    protected boolean ambientOcclusion = true;

    protected ModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation);
        this.existingFileHelper = existingFileHelper;
    }

    @SuppressWarnings("unchecked")
    private T self() { return (T) this; }

    public T parent(ModelFile parent) {
        this.parent = parent;
        return self();
    }

    public T texture(String key, String texture) {
        this.textures.put(key, texture);
        return self();
    }

    public T texture(String key, ResourceLocation texture) {
        return texture(key, texture.toString());
    }

    public T renderType(String renderType) {
        this.renderType = renderType;
        return self();
    }

    public T renderType(ResourceLocation renderType) {
        return renderType(renderType.toString());
    }

    public T ao(boolean ao) {
        this.ambientOcclusion = ao;
        return self();
    }

    @Override
    protected boolean exists() {
        return true;
    }

    /**
     * Serializes this model builder to a JSON object for data generation.
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (parent != null) {
            json.addProperty("parent", parent.getLocation().toString());
        }
        if (!textures.isEmpty()) {
            JsonObject tex = new JsonObject();
            textures.forEach(tex::addProperty);
            json.add("textures", tex);
        }
        if (renderType != null) {
            json.addProperty("render_type", renderType);
        }
        if (!ambientOcclusion) {
            json.addProperty("ambientocclusion", false);
        }
        return json;
    }

    public static class ElementBuilder {
        public static class FaceBuilder {
        }
        public static class RotationBuilder {
        }
    }

    public enum FaceRotation { ZERO, CW_90, CCW_90, UPSIDE_DOWN }

    public static class TransformsBuilder {
        public static class TransformVecBuilder {
        }
    }
}
