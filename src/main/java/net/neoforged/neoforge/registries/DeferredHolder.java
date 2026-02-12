package net.neoforged.neoforge.registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Proxy for NeoForge's {@code DeferredHolder}.
 * NeoForge renamed {@code RegistryObject} → {@code DeferredHolder}.
 *
 * <p>Supports two modes:</p>
 * <ul>
 *   <li><b>Wrapped:</b> delegates to a Forge {@link RegistryObject} for standard registries.</li>
 *   <li><b>Direct:</b> holds a value directly for NeoForge-only registries that don't
 *       exist in Forge (e.g. {@code neoforge:attachment_types}).</li>
 * </ul>
 *
 * @param <R> the registry supertype
 * @param <T> the specific object type
 */
public class DeferredHolder<R, T extends R> implements Supplier<T> {

    private final RegistryObject<T> delegate;     // non-null in wrapped mode
    private final ResourceLocation directId;       // non-null in direct mode
    private final Supplier<? extends T> directSup; // non-null in direct mode
    private T directCached;

    /** Wrapped mode — backed by a Forge RegistryObject. */
    private DeferredHolder(RegistryObject<T> delegate) {
        this.delegate = delegate;
        this.directId = null;
        this.directSup = null;
    }

    /** Direct mode — backed by a supplier (for no-op registries). */
    private DeferredHolder(ResourceLocation id, Supplier<? extends T> supplier) {
        this.delegate = null;
        this.directId = id;
        this.directSup = supplier;
    }

    /**
     * Wrap a Forge RegistryObject into a DeferredHolder.
     */
    public static <R, T extends R> DeferredHolder<R, T> wrap(RegistryObject<T> obj) {
        return new DeferredHolder<>(obj);
    }

    /**
     * Create a DeferredHolder that directly evaluates a supplier (no Forge registry).
     */
    public static <R, T extends R> DeferredHolder<R, T> createDirect(ResourceLocation id,
                                                                       Supplier<? extends T> supplier) {
        return new DeferredHolder<>(id, supplier);
    }

    /**
     * Get the registered value. (Forge-compatible)
     */
    @Override
    public T get() {
        if (delegate != null) return delegate.get();
        if (directCached == null && directSup != null) {
            directCached = directSup.get();
        }
        return directCached;
    }

    /**
     * Get the registered value. (NeoForge name)
     */
    public T value() {
        return get();
    }

    /**
     * Get the registry name.
     */
    public ResourceLocation getId() {
        if (delegate != null) return delegate.getId();
        return directId;
    }

    /**
     * Check if the value is present.
     */
    public boolean isPresent() {
        if (delegate != null) return delegate.isPresent();
        return true;
    }

    /**
     * Get the underlying Forge RegistryObject (null in direct mode).
     */
    public RegistryObject<T> asRegistryObject() {
        return delegate;
    }
}
