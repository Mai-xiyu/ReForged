package net.neoforged.neoforge.client.model.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModelData {
    public static final ModelData EMPTY = new ModelData(Collections.emptyMap());

    private final Map<ModelProperty<?>, Object> properties;

    private ModelData(Map<ModelProperty<?>, Object> properties) {
        this.properties = properties;
    }

    @SuppressWarnings("unchecked")
    @javax.annotation.Nullable
    public <T> T get(ModelProperty<T> property) {
        return (T) properties.get(property);
    }

    public boolean has(ModelProperty<?> property) {
        return properties.containsKey(property);
    }

    /**
     * Creates a new builder pre-populated with this instance's properties,
     * allowing derivation of a new ModelData with additional or overridden values.
     */
    public Builder derive() {
        Builder b = new Builder();
        b.map.putAll(this.properties);
        return b;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<ModelProperty<?>, Object> map = new HashMap<>();

        public <T> Builder with(ModelProperty<T> property, T value) {
            map.put(property, value);
            return this;
        }

        public ModelData build() {
            return new ModelData(Collections.unmodifiableMap(new HashMap<>(map)));
        }
    }
}
