package net.neoforged.neoforge.common.conditions;

/** Proxy: NeoForge's ICondition interface for conditional loading */
public interface ICondition {
    boolean test(Object context);
}
