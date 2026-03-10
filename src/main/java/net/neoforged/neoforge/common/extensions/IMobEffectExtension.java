package net.neoforged.neoforge.common.extensions;

import java.util.Set;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;

/**
 * Extension interface for {@link MobEffect}.
 */
public interface IMobEffectExtension {

    private MobEffect self() { return (MobEffect) this; }

    /**
     * Fills the set of cures this effect responds to.
     */
    default void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        cures.add(EffectCures.MILK);
    }

    default int getSortOrder(MobEffectInstance effectInstance) {
        return 0;
    }
}
