package net.neoforged.neoforge.client.extensions;

/**
 * Extension interface for model state (transform) objects.
 */
public interface ModelStateExtension {

    /**
     * Returns whether this model state may apply non-90-degree rotations.
     * BlockModelRotation returns false, other implementations return true.
     */
    default boolean mayApplyArbitraryRotation() {
        return true;
    }
}
