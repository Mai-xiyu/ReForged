package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;

/**
 * Condition that negates another condition.
 */
public record NotCondition(ICondition value) implements ICondition {
    @Override
    public boolean test(IContext context) {
        return !value.test(context);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return MapCodec.unit(this);
    }
}
