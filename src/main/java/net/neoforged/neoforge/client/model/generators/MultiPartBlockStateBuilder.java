package net.neoforged.neoforge.client.model.generators;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Builder for multipart-style block states.
 */
public final class MultiPartBlockStateBuilder implements IGeneratedBlockState {
    private final Block owner;
    private final List<PartBuilder> parts = new ArrayList<>();

    public MultiPartBlockStateBuilder(Block owner) {
        this.owner = owner;
    }

    public Block getOwner() { return owner; }

    public ConfiguredModel.Builder<PartBuilder> part() {
        PartBuilder partBuilder = new PartBuilder();
        parts.add(partBuilder);
        return ConfiguredModel.builderForPart(partBuilder);
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        JsonArray multipart = new JsonArray();
        for (PartBuilder part : parts) {
            multipart.add(part.toJson());
        }
        root.add("multipart", multipart);
        return root;
    }

    public static class PartBuilder {
        public ConfiguredModel[] models;
        public boolean useOr = false;
        public final Multimap<Property<?>, Comparable<?>> conditions = ArrayListMultimap.create();
        public final List<ConditionGroup> nestedConditionGroups = new ArrayList<>();

        public PartBuilder useOr() {
            this.useOr = true;
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> PartBuilder condition(Property<T> prop, T... values) {
            for (T value : values) {
                conditions.put(prop, value);
            }
            return this;
        }

        public ConditionGroup nestedGroup() {
            ConditionGroup group = new ConditionGroup(this, null);
            nestedConditionGroups.add(group);
            return group;
        }

        public MultiPartBlockStateBuilder end() {
            return null; // Set by ConfiguredModel.builderForPart
        }

        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            if (models != null && models.length > 0) {
                obj.add("apply", ConfiguredModel.toJSON(models));
            }
            return obj;
        }

        public static class ConditionGroup {
            public final Multimap<Property<?>, Comparable<?>> conditions = ArrayListMultimap.create();
            public final List<ConditionGroup> nestedConditionGroups = new ArrayList<>();
            public boolean useOr = false;
            private final PartBuilder parent;
            private final ConditionGroup parentGroup;

            public ConditionGroup(PartBuilder parent, ConditionGroup parentGroup) {
                this.parent = parent;
                this.parentGroup = parentGroup;
            }

            @SafeVarargs
            public final <T extends Comparable<T>> ConditionGroup condition(Property<T> prop, T... values) {
                for (T value : values) {
                    conditions.put(prop, value);
                }
                return this;
            }

            public ConditionGroup nestedGroup() {
                ConditionGroup group = new ConditionGroup(parent, this);
                nestedConditionGroups.add(group);
                return group;
            }

            public ConditionGroup endNestedGroup() {
                return parentGroup;
            }

            public PartBuilder end() {
                return parent;
            }

            public ConditionGroup useOr() {
                this.useOr = true;
                return this;
            }
        }
    }
}
