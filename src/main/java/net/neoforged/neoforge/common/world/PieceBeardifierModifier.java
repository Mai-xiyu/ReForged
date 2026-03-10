package net.neoforged.neoforge.common.world;

import net.minecraft.world.level.levelgen.Beardifier;

/**
 * Interface for structure pieces that modify the beardifier noise.
 * Allows structure pieces to customize terrain carving/filling around them.
 */
public interface PieceBeardifierModifier {
    /**
     * Returns the beardifier contribution of this structure piece at the given position.
     */
    default Beardifier.Rigid getBeardifierContribution() {
        return null;
    }
}
