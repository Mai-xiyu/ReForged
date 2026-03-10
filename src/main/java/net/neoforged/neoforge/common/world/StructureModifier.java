package net.neoforged.neoforge.common.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Interface for modifying structures during worldgen.
 */
public interface StructureModifier {
    /**
     * Modifies structure settings.
     *
     * @param structure the structure being modified
     * @param phase     the phase of modification
     * @param builder   the builder to apply modifications to
     */
    void modify(Holder<Structure> structure, Phase phase, ModifiableStructureInfo.StructureInfo.Builder builder);

    /**
     * Returns the codec for this structure modifier type.
     */
    MapCodec<? extends StructureModifier> codec();

    enum Phase {
        ADD,
        REMOVE,
        MODIFY,
        AFTER_EVERYTHING
    }
}
