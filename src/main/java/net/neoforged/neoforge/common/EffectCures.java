package net.neoforged.neoforge.common;

/**
 * Standard effect cure constants.
 */
public class EffectCures {
    private EffectCures() {}

    /** Cure by drinking milk. */
    public static final EffectCure MILK = EffectCure.get("milk");
    /** Cure by eating a golden apple. */
    public static final EffectCure PROTECTED_BY_TOTEM = EffectCure.get("protected_by_totem");
    /** Cure by drinking honey. */
    public static final EffectCure HONEY = EffectCure.get("honey");
}
