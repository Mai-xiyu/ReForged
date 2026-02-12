package net.neoforged.neoforge.common.loot;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Proxy: NeoForge's LootModifier base class.
 * Extends Forge's LootModifier to inherit {@code codecStart()}, constructor,
 * and the full {@code apply()} / {@code doApply()} lifecycle.
 */
public abstract class LootModifier extends net.minecraftforge.common.loot.LootModifier
        implements IGlobalLootModifier {

    protected LootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }
}
