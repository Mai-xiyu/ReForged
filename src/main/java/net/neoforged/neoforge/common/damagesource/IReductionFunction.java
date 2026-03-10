package net.neoforged.neoforge.common.damagesource;

/**
 * Used by {@link DamageContainer} instances to sequentially modify damage reduction values.
 */
@FunctionalInterface
public interface IReductionFunction {
    /**
     * Modifies a reduction value for the given damage container.
     *
     * @param container   the DamageContainer representing the damage sequence
     * @param reductionIn the initial or preceding reduction value
     * @return the new reduction value
     */
    float modify(DamageContainer container, float reductionIn);
}
