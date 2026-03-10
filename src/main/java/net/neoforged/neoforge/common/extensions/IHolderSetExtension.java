package net.neoforged.neoforge.common.extensions;

/**
 * Extension interface for HolderSet.
 */
public interface IHolderSetExtension<T> {

    /**
     * Adds a callback to run when this holderset's contents invalidate.
     */
    default void addInvalidationListener(Runnable runnable) {
        // noop by default
    }

    /**
     * Returns the serialization type of this holder set.
     */
    default SerializationType serializationType() {
        return SerializationType.UNKNOWN;
    }

    /**
     * What format a holderset serializes to in json/nbt/etc
     */
    enum SerializationType {
        /** Unhandled/unsupported holderset implementation, could serialize as potentially anything **/
        UNKNOWN,
        STRING,
        LIST,
        OBJECT
    }
}
