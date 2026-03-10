package net.neoforged.neoforge.capabilities;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Registry for all NeoForge-style capabilities.
 * Keeps track of all created {@link BaseCapability} instances.
 */
public final class CapabilityRegistry {
    private static final Set<BaseCapability<?, ?>> ALL_CAPABILITIES = Collections.synchronizedSet(
            Collections.newSetFromMap(new IdentityHashMap<>()));

    private CapabilityRegistry() {}

    /**
     * Registers a new capability. Called internally by {@link BaseCapability} subclass factories.
     */
    @ApiStatus.Internal
    public static void register(BaseCapability<?, ?> capability) {
        ALL_CAPABILITIES.add(capability);
    }

    /**
     * Returns an unmodifiable view of all registered capabilities.
     */
    public static Set<BaseCapability<?, ?>> getAll() {
        return Collections.unmodifiableSet(ALL_CAPABILITIES);
    }
}
