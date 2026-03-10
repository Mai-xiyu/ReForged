package net.neoforged.neoforge.common;

import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a method of curing a potion effect (e.g. drinking milk).
 */
public final class EffectCure {
    private static final Map<String, EffectCure> CURES = new ConcurrentHashMap<>();
    private final String name;

    private EffectCure(String name) {
        this.name = name;
    }

    /**
     * Gets or creates an EffectCure with the given name.
     */
    public static EffectCure get(String name) {
        return CURES.computeIfAbsent(name, EffectCure::new);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "EffectCure[" + name + "]";
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
