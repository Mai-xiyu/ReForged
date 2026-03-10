package net.neoforged.neoforge.registries.datamaps;

import net.neoforged.bus.api.Event;

/**
 * Fired when data maps have been updated/reloaded.
 */
public class DataMapsUpdatedEvent extends Event {
    private final boolean fromDatapack;

    public DataMapsUpdatedEvent(boolean fromDatapack) {
        this.fromDatapack = fromDatapack;
    }

    public DataMapsUpdatedEvent() {
        this(false);
    }

    public boolean isFromDatapack() { return fromDatapack; }
}
