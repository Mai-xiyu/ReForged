package net.neoforged.neoforge.common.world;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for MobSpawnSettings that allows biome modifiers to add/remove spawns.
 */
public class MobSpawnSettingsBuilder {
    private final Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawners = new EnumMap<>(MobCategory.class);
    private float creatureGenerationProbability = 0.1F;

    public MobSpawnSettingsBuilder() {
        for (MobCategory category : MobCategory.values()) {
            spawners.put(category, new ArrayList<>());
        }
    }

    public MobSpawnSettingsBuilder(MobSpawnSettings spawns) {
        this();
        this.creatureGenerationProbability = spawns.getCreatureProbability();
        for (MobCategory category : MobCategory.values()) {
            spawners.get(category).addAll(spawns.getMobs(category).unwrap());
        }
    }

    public List<MobSpawnSettings.SpawnerData> getSpawner(MobCategory category) {
        return spawners.get(category);
    }

    public MobSpawnSettingsBuilder addSpawn(MobCategory category, MobSpawnSettings.SpawnerData data) {
        spawners.get(category).add(data);
        return this;
    }

    public MobSpawnSettingsBuilder removeSpawn(EntityType<?> entityType) {
        for (List<MobSpawnSettings.SpawnerData> list : spawners.values()) {
            list.removeIf(data -> data.type == entityType);
        }
        return this;
    }

    public float getCreatureGenerationProbability() {
        return creatureGenerationProbability;
    }

    public MobSpawnSettingsBuilder creatureGenerationProbability(float probability) {
        this.creatureGenerationProbability = probability;
        return this;
    }

    public MobSpawnSettings build() {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        builder.creatureGenerationProbability(creatureGenerationProbability);
        for (Map.Entry<MobCategory, List<MobSpawnSettings.SpawnerData>> entry : spawners.entrySet()) {
            for (MobSpawnSettings.SpawnerData data : entry.getValue()) {
                builder.addSpawn(entry.getKey(), data);
            }
        }
        return builder.build();
    }
}
