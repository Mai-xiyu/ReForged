package net.neoforged.neoforge.common.world;

import com.mojang.serialization.MapCodec;

/**
 * Built-in structure modifier implementations.
 */
public class StructureModifiers {
    /**
     * No-op structure modifier that does nothing.
     */
    public static final StructureModifier NONE = new NoneStructureModifier();

    public static final MapCodec<NoneStructureModifier> NONE_CODEC = MapCodec.unit(NoneStructureModifier::new);

    static class NoneStructureModifier implements StructureModifier {
        @Override
        public void modify(net.minecraft.core.Holder<net.minecraft.world.level.levelgen.structure.Structure> structure,
                Phase phase, ModifiableStructureInfo.StructureInfo.Builder builder) {
            // No-op
        }

        @Override
        public MapCodec<? extends StructureModifier> codec() {
            return NONE_CODEC;
        }
    }
}
