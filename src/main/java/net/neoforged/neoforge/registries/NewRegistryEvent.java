package net.neoforged.neoforge.registries;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fired to allow mods to register custom registries.
 * This is one of the first events fired during mod loading.
 */
public class NewRegistryEvent extends Event implements IModBusEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<Registry<?>> registries = new ArrayList<>();

    /**
     * Creates a new registry from the given builder.
     * @return the created registry
     */
    public <T> Registry<T> create(RegistryBuilder<T> builder) {
        Registry<T> registry = builder.create();
        register(registry);
        return registry;
    }

    /**
     * Registers an already-created registry.
     */
    public <T> void register(Registry<T> registry) {
        registries.add(registry);
        LOGGER.debug("[ReForged] NewRegistryEvent: registered custom registry '{}'", registry.key().location());
    }

    /**
     * Returns all registries created/registered during this event.
     */
    public List<Registry<?>> getRegistries() {
        return Collections.unmodifiableList(registries);
    }
}
