package net.neoforged.neoforge.common.world;

import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Modifiable wrapper for structure info, used by the structure modifier system.
 */
public record ModifiableStructureInfo(StructureInfo originalStructureInfo) {

    /**
     * Holder for the full structure info.
     */
    public record StructureInfo(Structure.StructureSettings settings) {
        /**
         * Creates a builder from this info for modification.
         */
        public Builder toBuilder() {
            return new Builder(settings);
        }

        /**
         * Builder for StructureInfo that structure modifiers use to apply changes.
         */
        public static class Builder {
            private Structure.StructureSettings settings;

            public Builder(Structure.StructureSettings settings) {
                this.settings = settings;
            }

            public Structure.StructureSettings getStructureSettings() { return settings; }

            public StructureInfo build() {
                return new StructureInfo(settings);
            }
        }
    }
}
