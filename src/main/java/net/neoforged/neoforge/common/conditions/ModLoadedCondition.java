package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Condition that tests whether a specific mod is loaded.
 */
public record ModLoadedCondition(String modId) implements ICondition {
    public static final MapCodec<ModLoadedCondition> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(com.mojang.serialization.Codec.STRING.fieldOf("modid").forGetter(ModLoadedCondition::modId))
                    .apply(inst, ModLoadedCondition::new));

    @Override
    public boolean test(IContext context) {
        return net.minecraftforge.fml.ModList.get().isLoaded(modId);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
