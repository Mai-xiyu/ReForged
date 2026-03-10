package net.neoforged.neoforge.client.model.generators;

import com.google.gson.JsonObject;
import java.util.*;
import java.util.function.Function;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Builder for variant-style block states.
 */
public class VariantBlockStateBuilder implements IGeneratedBlockState {
    private final Block owner;
    private final Map<PartialBlockstate, ConfiguredModel[]> models = new LinkedHashMap<>();
    private final Set<BlockState> coveredStates = new HashSet<>();

    public VariantBlockStateBuilder(Block owner) {
        this.owner = owner;
    }

    public Block getOwner() { return owner; }

    public Map<PartialBlockstate, ConfiguredModel[]> getModels() {
        return Collections.unmodifiableMap(models);
    }

    public PartialBlockstate partialState() {
        return new PartialBlockstate(this);
    }

    public VariantBlockStateBuilder addModels(PartialBlockstate state, ConfiguredModel... models) {
        this.models.put(state, models);
        return this;
    }

    public VariantBlockStateBuilder setModels(PartialBlockstate state, ConfiguredModel... models) {
        this.models.put(state, models);
        return this;
    }

    public VariantBlockStateBuilder forAllStates(Function<BlockState, ConfiguredModel[]> mapper) {
        for (BlockState state : owner.getStateDefinition().getPossibleStates()) {
            PartialBlockstate partial = new PartialBlockstate(this);
            models.put(partial, mapper.apply(state));
            coveredStates.add(state);
        }
        return this;
    }

    @SafeVarargs
    public final VariantBlockStateBuilder forAllStatesExcept(Function<BlockState, ConfiguredModel[]> mapper, Property<?>... ignored) {
        return forAllStates(mapper);
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        for (Map.Entry<PartialBlockstate, ConfiguredModel[]> entry : models.entrySet()) {
            // Simplified serialization
            variants.add(entry.getKey().toString(), ConfiguredModel.toJSON(entry.getValue()));
        }
        root.add("variants", variants);
        return root;
    }

    public static class PartialBlockstate {
        private final VariantBlockStateBuilder parent;
        private final SortedMap<Property<?>, Comparable<?>> setStates = new TreeMap<>(Comparator.comparing(Property::getName));

        public PartialBlockstate(VariantBlockStateBuilder parent) {
            this.parent = parent;
        }

        public <T extends Comparable<T>> PartialBlockstate with(Property<T> prop, T value) {
            setStates.put(prop, value);
            return this;
        }

        public ConfiguredModel.Builder<VariantBlockStateBuilder> modelForState() {
            return ConfiguredModel.builder(parent, this);
        }

        public PartialBlockstate addModels(ConfiguredModel... models) {
            parent.addModels(this, models);
            return this;
        }

        public VariantBlockStateBuilder setModels(ConfiguredModel... models) {
            return parent.setModels(this, models);
        }

        public PartialBlockstate partialState() {
            return parent.partialState();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Property<?>, Comparable<?>> entry : setStates.entrySet()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(entry.getKey().getName()).append("=").append(entry.getValue());
            }
            return sb.toString();
        }
    }
}
