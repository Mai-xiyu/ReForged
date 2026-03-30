package net.neoforged.neoforge.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Built-in biome modifier implementations.
 */
public final class BiomeModifiers {
    private BiomeModifiers() {}

    public record AddFeaturesBiomeModifier(
            HolderSet<Biome> biomes,
            HolderSet<PlacedFeature> features,
            GenerationStep.Decoration step
    ) implements BiomeModifier {

        public static final MapCodec<AddFeaturesBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(AddFeaturesBiomeModifier::biomes),
                RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE).fieldOf("features").forGetter(AddFeaturesBiomeModifier::features),
                GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(AddFeaturesBiomeModifier::step)
        ).apply(inst, AddFeaturesBiomeModifier::new));

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD && biomes.contains(biome)) {
                BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
                features.forEach(holder -> generationSettings.addFeature(step, holder));
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return CODEC;
        }
    }

    public record RemoveFeaturesBiomeModifier(
            HolderSet<Biome> biomes,
            HolderSet<PlacedFeature> features,
            Set<GenerationStep.Decoration> steps
    ) implements BiomeModifier {

        public static final MapCodec<RemoveFeaturesBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(RemoveFeaturesBiomeModifier::biomes),
                RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE).fieldOf("features").forGetter(RemoveFeaturesBiomeModifier::features),
                GenerationStep.Decoration.CODEC.listOf().<Set<GenerationStep.Decoration>>xmap(
                        list -> list.isEmpty() ? EnumSet.noneOf(GenerationStep.Decoration.class) : EnumSet.copyOf(list),
                        set -> List.copyOf(set)
                ).fieldOf("steps").forGetter(RemoveFeaturesBiomeModifier::steps)
        ).apply(inst, RemoveFeaturesBiomeModifier::new));

        public static RemoveFeaturesBiomeModifier allSteps(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features) {
            return new RemoveFeaturesBiomeModifier(biomes, features, EnumSet.allOf(GenerationStep.Decoration.class));
        }

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.REMOVE && biomes.contains(biome)) {
                BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
                for (GenerationStep.Decoration step : steps) {
                    generationSettings.getFeatures(step).removeIf(features::contains);
                }
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return CODEC;
        }
    }

    public record AddSpawnsBiomeModifier(
            HolderSet<Biome> biomes,
            List<MobSpawnSettings.SpawnerData> spawners
    ) implements BiomeModifier {

        public static final MapCodec<AddSpawnsBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(AddSpawnsBiomeModifier::biomes),
                MobSpawnSettings.SpawnerData.CODEC.listOf().fieldOf("spawners").forGetter(AddSpawnsBiomeModifier::spawners)
        ).apply(inst, AddSpawnsBiomeModifier::new));

        public static AddSpawnsBiomeModifier singleSpawn(HolderSet<Biome> biomes, MobSpawnSettings.SpawnerData spawner) {
            return new AddSpawnsBiomeModifier(biomes, List.of(spawner));
        }

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD && biomes.contains(biome)) {
                MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
                for (MobSpawnSettings.SpawnerData spawner : spawners) {
                    spawns.addSpawn(spawner.type.getCategory(), spawner);
                }
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return CODEC;
        }
    }

    public record RemoveSpawnsBiomeModifier(
            HolderSet<Biome> biomes,
            HolderSet<EntityType<?>> entityTypes
    ) implements BiomeModifier {

        public static final MapCodec<RemoveSpawnsBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(RemoveSpawnsBiomeModifier::biomes),
                RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity_types").forGetter(RemoveSpawnsBiomeModifier::entityTypes)
        ).apply(inst, RemoveSpawnsBiomeModifier::new));

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.REMOVE && biomes.contains(biome)) {
                MobSpawnSettingsBuilder spawnBuilder = builder.getMobSpawnSettings();
                for (MobCategory category : MobCategory.values()) {
                    List<MobSpawnSettings.SpawnerData> spawns = spawnBuilder.getSpawner(category);
                    spawns.removeIf(spawnerData -> entityTypes.contains(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(spawnerData.type)));
                }
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return CODEC;
        }
    }
}
