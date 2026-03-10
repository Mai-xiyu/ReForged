package net.neoforged.neoforge.common.world;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.*;

/**
 * Builder for BiomeGenerationSettings that allows biome modifiers to add/remove features and carvers.
 */
public class BiomeGenerationSettingsBuilder {
    private final Map<GenerationStep.Decoration, List<Holder<PlacedFeature>>> features = new EnumMap<>(GenerationStep.Decoration.class);
    private final Map<GenerationStep.Carving, List<Holder<ConfiguredWorldCarver<?>>>> carvers = new EnumMap<>(GenerationStep.Carving.class);

    public BiomeGenerationSettingsBuilder() {
        for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
            features.put(step, new ArrayList<>());
        }
        for (GenerationStep.Carving step : GenerationStep.Carving.values()) {
            carvers.put(step, new ArrayList<>());
        }
    }

    public BiomeGenerationSettingsBuilder(BiomeGenerationSettings original) {
        this();
        for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
            List<HolderSet<PlacedFeature>> stepFeatures = original.features();
            int idx = step.ordinal();
            if (idx < stepFeatures.size()) {
                stepFeatures.get(idx).forEach(h -> features.get(step).add(h));
            }
        }
        for (GenerationStep.Carving step : GenerationStep.Carving.values()) {
            original.getCarvers(step).forEach(h -> carvers.get(step).add(h));
        }
    }

    public List<Holder<PlacedFeature>> getFeatures(GenerationStep.Decoration step) {
        return features.get(step);
    }

    public BiomeGenerationSettingsBuilder addFeature(GenerationStep.Decoration step, Holder<PlacedFeature> feature) {
        features.get(step).add(feature);
        return this;
    }

    public List<Holder<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving step) {
        return carvers.get(step);
    }

    public BiomeGenerationSettingsBuilder addCarver(GenerationStep.Carving step, Holder<ConfiguredWorldCarver<?>> carver) {
        carvers.get(step).add(carver);
        return this;
    }

    public BiomeGenerationSettings build() {
        BiomeGenerationSettings.PlainBuilder builder = new BiomeGenerationSettings.PlainBuilder();
        for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
            for (Holder<PlacedFeature> feature : features.get(step)) {
                builder.addFeature(step, feature);
            }
        }
        for (GenerationStep.Carving step : GenerationStep.Carving.values()) {
            for (Holder<ConfiguredWorldCarver<?>> carver : carvers.get(step)) {
                builder.addCarver(step, carver);
            }
        }
        return builder.build();
    }
}
