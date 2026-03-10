package net.neoforged.neoforge.common.world;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.EnumMap;
import java.util.List;

/**
 * Modifiable wrapper for biome info, used by the biome modifier system.
 */
public record ModifiableBiomeInfo(BiomeInfo originalBiomeInfo) {

    /**
     * Holder for the full biome info: climate, effects, generation, mob spawns.
     */
    public record BiomeInfo(
            Biome.ClimateSettings climateSettings,
            BiomeSpecialEffects effects,
            BiomeGenerationSettings generationSettings,
            MobSpawnSettings mobSpawnSettings
    ) {
        /**
         * Creates a builder from this info for modification.
         */
        public Builder toBuilder() {
            return new Builder(climateSettings, effects, generationSettings, mobSpawnSettings);
        }

        /**
         * Builder for BiomeInfo that biome modifiers use to apply changes.
         */
        public static class Builder {
            private Biome.ClimateSettings climateSettings;
            private BiomeSpecialEffects effects;
            private final BiomeGenerationSettingsBuilder generationSettings;
            private final MobSpawnSettingsBuilder mobSpawnSettings;

            public Builder(Biome.ClimateSettings climate, BiomeSpecialEffects effects,
                    BiomeGenerationSettings gen, MobSpawnSettings spawns) {
                this.climateSettings = climate;
                this.effects = effects;
                this.generationSettings = new BiomeGenerationSettingsBuilder(gen);
                this.mobSpawnSettings = new MobSpawnSettingsBuilder(spawns);
            }

            public Biome.ClimateSettings getClimateSettings() { return climateSettings; }
            public void setClimateSettings(Biome.ClimateSettings climateSettings) { this.climateSettings = climateSettings; }
            public BiomeSpecialEffects getSpecialEffects() { return effects; }
            public void setSpecialEffects(BiomeSpecialEffects effects) { this.effects = effects; }
            public BiomeGenerationSettingsBuilder getGenerationSettings() { return generationSettings; }
            public MobSpawnSettingsBuilder getMobSpawnSettings() { return mobSpawnSettings; }

            public BiomeInfo build() {
                return new BiomeInfo(climateSettings, effects, generationSettings.build(), mobSpawnSettings.build());
            }
        }
    }
}
