package net.neoforged.neoforge.client.model.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * NeoForge ConfiguredModel — represents a model with rotation/UV lock configuration.
 */
public class ConfiguredModel {
    public final ModelFile model;
    public final int rotationX;
    public final int rotationY;
    public final boolean uvLock;
    public final int weight;

    public ConfiguredModel(ModelFile model) {
        this(model, 0, 0, false, 1);
    }

    public ConfiguredModel(ModelFile model, int rotationX, int rotationY, boolean uvLock, int weight) {
        this.model = model;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.uvLock = uvLock;
        this.weight = weight;
    }

    public JsonObject toJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("model", model.getLocation().toString());
        if (rotationX != 0) obj.addProperty("x", rotationX);
        if (rotationY != 0) obj.addProperty("y", rotationY);
        if (uvLock) obj.addProperty("uvlock", true);
        if (weight != 1) obj.addProperty("weight", weight);
        return obj;
    }

    public static JsonElement toJSON(ConfiguredModel[] models) {
        if (models.length == 1) {
            return models[0].toJSON();
        }
        JsonArray arr = new JsonArray();
        for (ConfiguredModel m : models) {
            arr.add(m.toJSON());
        }
        return arr;
    }

    public static ConfiguredModel[] allRotations(ModelFile model, boolean uvlock) {
        return new ConfiguredModel[]{new ConfiguredModel(model)};
    }

    public static ConfiguredModel[] allYRotations(ModelFile model, int x, boolean uvlock) {
        ConfiguredModel[] models = new ConfiguredModel[4];
        for (int i = 0; i < 4; i++) {
            models[i] = new ConfiguredModel(model, x, i * 90, uvlock, 1);
        }
        return models;
    }

    public static <T> Builder<T> builder(T parent, VariantBlockStateBuilder.PartialBlockstate state) {
        return new Builder<>(parent, models -> state.addModels(models));
    }

    public static Builder<MultiPartBlockStateBuilder.PartBuilder> builderForPart(MultiPartBlockStateBuilder.PartBuilder part) {
        return new Builder<>(part, models -> part.models = models);
    }

    public static Builder<?> builder() {
        return new Builder<>(null, m -> {});
    }

    public static class Builder<T> {
        private ModelFile model;
        private int rotationX;
        private int rotationY;
        private boolean uvLock;
        private int weight = 1;
        private final T parent;
        private final java.util.function.Consumer<ConfiguredModel[]> callback;

        Builder(T parent, java.util.function.Consumer<ConfiguredModel[]> callback) {
            this.parent = parent;
            this.callback = callback;
        }

        public Builder<T> modelFile(ModelFile model) { this.model = model; return this; }
        public Builder<T> rotationX(int x) { this.rotationX = x; return this; }
        public Builder<T> rotationY(int y) { this.rotationY = y; return this; }
        public Builder<T> uvLock(boolean uvLock) { this.uvLock = uvLock; return this; }
        public Builder<T> weight(int weight) { this.weight = weight; return this; }

        public ConfiguredModel[] buildLast() {
            return new ConfiguredModel[]{new ConfiguredModel(model, rotationX, rotationY, uvLock, weight)};
        }

        @SuppressWarnings("unchecked")
        public T addModel() {
            callback.accept(buildLast());
            return parent;
        }
    }
}
