package net.neoforged.neoforge.client.extensions;

/**
 * Extension interface for {@link net.minecraft.client.KeyMapping}.
 */
public interface IKeyMappingExtension {

    /**
     * Returns true if this key mapping conflicts with the given mapping.
     */
    default boolean isConflictContextAndModifierActive() {
        return true;
    }
}
