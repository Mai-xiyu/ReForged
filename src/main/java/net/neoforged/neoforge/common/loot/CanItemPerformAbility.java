package net.neoforged.neoforge.common.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.ToolAction;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Loot condition that checks if a tool can perform a given ability.
 */
public class CanItemPerformAbility implements LootItemCondition {
    public static final MapCodec<CanItemPerformAbility> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    ItemAbility.CODEC.fieldOf("ability").forGetter(c -> c.ability)
            ).apply(inst, CanItemPerformAbility::new)
    );

    public static final LootItemConditionType LOOT_CONDITION_TYPE = new LootItemConditionType(CODEC);

    final ItemAbility ability;

    public CanItemPerformAbility(ItemAbility ability) {
        this.ability = ability;
    }

    @Override
    public LootItemConditionType getType() {
        return LOOT_CONDITION_TYPE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override
    public boolean test(LootContext context) {
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        return tool != null && tool.canPerformAction(ToolAction.get(ability.name()));
    }

    public static LootItemCondition.Builder canItemPerformAbility(ItemAbility ability) {
        return () -> new CanItemPerformAbility(ability);
    }
}
