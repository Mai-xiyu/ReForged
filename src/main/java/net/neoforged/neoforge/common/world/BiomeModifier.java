package net.neoforged.neoforge.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;

/**
 * Interface for modifying biomes during worldgen. Biome modifiers allow adding/removing
 * features, spawns, and other properties from biomes without directly editing biome JSON.
 */
public interface BiomeModifier {
    /**
     * Modifies a biome's generation and spawn settings.
     *
     * @param biome    the biome being modified
     * @param phase    the phase of modification
     * @param builder  the builder to apply modifications to
     */
    void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder);

    /**
     * Returns the codec for this biome modifier type.
     */
    MapCodec<? extends BiomeModifier> codec();

    enum Phase {
        /** Add features, spawns, etc. */
        ADD,
        /** Remove features, spawns, etc. */
        REMOVE,
        /** Modify existing settings. */
        MODIFY,
        /** Final pass. */
        AFTER_EVERYTHING
    }
}
