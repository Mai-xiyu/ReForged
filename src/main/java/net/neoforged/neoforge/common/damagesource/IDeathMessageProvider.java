package net.neoforged.neoforge.common.damagesource;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Provides custom death messages based on the available damage context.
 */
public interface IDeathMessageProvider {
    /**
     * Default death message provider — delegates to the entity's CombatTracker.
     */
    IDeathMessageProvider DEFAULT = (entity, lastEntry, sigFall) -> {
        // Delegate to vanilla's CombatTracker which handles all message types internally
        return entity.getCombatTracker().getDeathMessage();
    };

    /**
     * Computes the death message from the available context.
     *
     * @param entity              The entity being killed.
     * @param lastEntry           The last entry from the entity's CombatTracker.
     * @param mostSignificantFall The most significant fall inflicted, may be null.
     * @return The death message for the slain entity.
     */
    Component getDeathMessage(LivingEntity entity, CombatEntry lastEntry, @Nullable CombatEntry mostSignificantFall);
}
