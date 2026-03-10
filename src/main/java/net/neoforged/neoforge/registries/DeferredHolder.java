package net.neoforged.neoforge.registries;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
 * <p>Supports late registration: if entries are added to a DeferredRegister after
 * the corresponding RegisterEvent has already fired, the supplier is evaluated
 * directly and the value is late-registered into the vanilla registry.</p>
 *
 * @param <R> the registry supertype
 * @param <T> the specific object type
 */
public class DeferredHolder<R, T extends R> implements Holder<T> {

    private static final Logger LOGGER = LogUtils.getLogger();

    protected final RegistryObject<T> delegate;     // non-null in wrapped mode
    private final ResourceLocation directId;       // non-null in direct mode
    private final Supplier<? extends T> directSup; // non-null in direct mode
    private final Supplier<? extends T> fallbackSup; // fallback for late-registered entries
    private ResourceKey<R> storedKey;              // cached ResourceKey (all modes)
    private volatile T directCached;
    protected boolean isNoOp;

    /** Wrapped mode — backed by a Forge RegistryObject. */
    DeferredHolder(RegistryObject<T> delegate) {
        this(delegate, null);
    }

    /** Wrapped mode with fallback supplier — for late registration support. */
    @SuppressWarnings("unchecked")
    DeferredHolder(RegistryObject<T> delegate, Supplier<? extends T> fallbackSup) {
        this.delegate = delegate;
        this.directId = null;
        this.directSup = null;
        this.fallbackSup = fallbackSup;
        this.storedKey = delegate != null ? (ResourceKey<R>) delegate.getKey() : null;
    }

    /** Direct mode — backed by a supplier (for no-op registries). */
    DeferredHolder(ResourceLocation id, Supplier<? extends T> supplier) {
        this.delegate = null;
        this.directId = id;
        this.directSup = supplier;
        this.fallbackSup = null;
        this.storedKey = null;
    }

    /** Direct mode with explicit ResourceKey — for no-op registries with key tracking. */
    DeferredHolder(ResourceKey<R> key, Supplier<? extends T> supplier) {
        this.delegate = null;
        this.directId = key != null ? key.location() : null;
        this.directSup = supplier;
        this.fallbackSup = null;
        this.storedKey = key;
    }

    /**
     * Protected constructor for subclasses (e.g. DeferredItem, DeferredBlock).
     * NeoForge API — lazy resolution via ResourceKey.
     * Handles null key gracefully (for RegistryEntry wrapping no-op holders).
     */
    @SuppressWarnings("unchecked")
    protected DeferredHolder(ResourceKey<R> key) {
        this.delegate = null;
        this.storedKey = key;
        if (key != null) {
            this.directId = key.location();
            this.directSup = () -> {
                var registry = (Registry<R>) BuiltInRegistries.REGISTRY.get(key.registry());
                if (registry != null) {
                    return (T) registry.get(key.location());
                }
                return null;
            };
        } else {
            this.directId = null;
            this.directSup = null;
        }
        this.fallbackSup = null;
    }

    /**
     * Wrap a Forge RegistryObject into a DeferredHolder (no fallback supplier).
     */
    public static <R, T extends R> DeferredHolder<R, T> wrap(RegistryObject<T> obj) {
        return new DeferredHolder<>(obj);
    }

    /**
     * Wrap a Forge RegistryObject into a DeferredHolder with a fallback supplier.
     * The fallback is used when the RegistryObject is not bound (late registration).
     */
    public static <R, T extends R> DeferredHolder<R, T> wrap(RegistryObject<T> obj, Supplier<? extends T> fallbackSup) {
        return new DeferredHolder<>(obj, fallbackSup);
    }

    /**
     * Create a DeferredHolder that directly evaluates a supplier (no Forge registry).
     */
    public static <R, T extends R> DeferredHolder<R, T> createDirect(ResourceLocation id,
                                                                       Supplier<? extends T> supplier) {
        return new DeferredHolder<>(id, supplier);
    }

    /**
     * Create a DeferredHolder that directly evaluates a supplier, with a full ResourceKey.
     * Used for no-op registries so getKey() returns a valid key.
     */
    public static <R, T extends R> DeferredHolder<R, T> createDirect(ResourceKey<R> key,
                                                                       Supplier<? extends T> supplier) {
        return new DeferredHolder<>(key, supplier);
    }

    /**
     * Create a DeferredHolder targeting a value by registry key and value name.
     * NeoForge API — resolves lazily through Forge's registry system.
     */
    @SuppressWarnings("unchecked")
    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation valueName) {
        return create(ResourceKey.create((ResourceKey<Registry<R>>) registryKey, valueName));
    }

    /**
     * Create a DeferredHolder targeting a value by registry name and value name.
     */
    public static <R, T extends R> DeferredHolder<R, T> create(ResourceLocation registryName, ResourceLocation valueName) {
        return create(ResourceKey.createRegistryKey(registryName), valueName);
    }

    /**
     * Create a DeferredHolder targeting a value by its full ResourceKey.
     */
    @SuppressWarnings("unchecked")
    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<R> key) {
        // Create a lazy holder — resolve at get() time from Forge's registry
        // Use the ResourceKey constructor to preserve storedKey for Registrate compatibility
        return new DeferredHolder<>(key, () -> {
            var registry = (Registry<R>) BuiltInRegistries.REGISTRY.get(key.registry());
            if (registry != null) {
                return (T) registry.get(key.location());
            }
            return null;
        });
    }

    /**
     * Get the registered value. (Forge-compatible)
     *
     * <p>If the underlying RegistryObject is not yet bound (late registration),
     * falls back to evaluating the supplier directly and attempting to
     * late-register the value into the vanilla registry.</p>
     */
    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        if (delegate != null) {
            if (delegate.isPresent()) {
                return delegate.get();
            }
            // Late registration fallback
            if (fallbackSup != null) {
                if (directCached == null) {
                    synchronized (this) {
                        if (directCached == null) {
                            LOGGER.info("[ReForged] Late-evaluating supplier for unbound RegistryObject: {}", delegate.getId());
                            T value = fallbackSup.get();
                            lateRegister(value);
                            directCached = value;
                        }
                    }
                }
                return directCached;
            }
            return delegate.get(); // will throw NPE if no fallback
        }
        if (directCached == null && directSup != null) {
            directCached = directSup.get();
        }
        return directCached;
    }

    /**
     * Try to late-register a value into the vanilla registry and update the RegistryObject.
     * This handles the case where entries are added to a DeferredRegister after
     * the corresponding RegisterEvent has already fired.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void lateRegister(T value) {
        if (value == null) return;
        ResourceKey<T> key = delegate.getKey();
        if (key == null) {
            LOGGER.warn("[ReForged] Cannot late-register: RegistryObject {} has no key", delegate.getId());
            return;
        }

        try {
            // 1. Try to register into the vanilla registry
            Registry<Object> registry = (Registry<Object>) BuiltInRegistries.REGISTRY.get(key.registry());
            if (registry != null && !registry.containsKey(key.location())) {
                // Temporarily unfreeze the registry
                boolean unfrozen = false;
                Field frozenField = null;
                try {
                    // Try official name first (dev environment), then SRG name (production)
                    frozenField = findField(registry.getClass(), "frozen", "f_205845_");
                    if (frozenField != null) {
                        frozenField.setAccessible(true);
                        if (frozenField.getBoolean(registry)) {
                            frozenField.setBoolean(registry, false);
                            unfrozen = true;
                        }
                    }
                    Registry.register((Registry) registry, key.location(), value);
                    LOGGER.info("[ReForged] Late-registered {} into registry {}", key.location(), key.registry());
                } catch (Exception e) {
                    LOGGER.warn("[ReForged] Could not late-register {} into registry {}: {}",
                            key.location(), key.registry(), e.getMessage());
                } finally {
                    // Re-freeze
                    if (unfrozen && frozenField != null) {
                        try {
                            frozenField.setBoolean(registry, true);
                        } catch (Exception ignored) {}
                    }
                }
            }

            // 2. Update the RegistryObject's value field so delegate.get() works next time
            try {
                Field valueField = RegistryObject.class.getDeclaredField("value");
                valueField.setAccessible(true);
                valueField.set(delegate, value);
                LOGGER.debug("[ReForged] Updated RegistryObject.value for {}", delegate.getId());
            } catch (Exception e) {
                LOGGER.debug("[ReForged] Could not update RegistryObject.value for {}: {}", delegate.getId(), e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.warn("[ReForged] Late registration failed for {}: {}", delegate.getId(), e.getMessage());
        }
    }

    /**
     * Find a declared field in the class hierarchy, trying multiple names (for SRG/official mapping support).
     */
    private static Field findField(Class<?> clazz, String... names) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (String name : names) {
                try {
                    return current.getDeclaredField(name);
                } catch (NoSuchFieldException ignored) {}
            }
            current = current.getSuperclass();
        }
        return null;
    }

    /* ---------- Holder<T> implementation ---------- */

    @Override
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
     * Get the ResourceKey for this holder.
     * NeoForge API — required by Registrate, Create and other mods.
     * Returns the stored key for all modes (wrapped, direct, no-op).
     */
    @SuppressWarnings("unchecked")
    public ResourceKey<R> getKey() {
        if (storedKey != null) return storedKey;
        // Fallback: try to derive from delegate
        if (delegate != null && delegate.getKey() != null) {
            storedKey = (ResourceKey<R>) delegate.getKey();
            return storedKey;
        }
        return null;
    }

    /**
     * Set the stored ResourceKey (used when the key wasn't available at construction time).
     */
    void setKey(ResourceKey<R> key) {
        this.storedKey = key;
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

    @Override
    public boolean isBound() {
        return isPresent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean is(ResourceLocation location) {
        ResourceLocation id = getId();
        return id != null && id.equals(location);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean is(ResourceKey<T> key) {
        ResourceKey<R> myKey = getKey();
        return myKey != null && myKey.equals(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean is(Predicate<ResourceKey<T>> predicate) {
        ResourceKey<R> myKey = getKey();
        return myKey != null && predicate.test((ResourceKey<T>) myKey);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean is(TagKey<T> tagKey) {
        ResourceKey key = getKey();
        if (key == null) return false;
        Registry registry = (Registry) BuiltInRegistries.REGISTRY.get(key.registry());
        if (registry == null) return false;
        Optional<Holder.Reference> opt = registry.getHolder(key);
        return opt.map(h -> h.is(tagKey)).orElse(false);
    }

    @Override
    public boolean is(Holder<T> other) {
        if (other == this) return true;
        return other.unwrapKey().isPresent() && this.unwrapKey().isPresent()
                && other.unwrapKey().get().equals(this.unwrapKey().get());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Stream<TagKey<T>> tags() {
        ResourceKey key = getKey();
        if (key == null) return Stream.empty();
        Registry registry = (Registry) BuiltInRegistries.REGISTRY.get(key.registry());
        if (registry == null) return Stream.empty();
        Optional<Holder.Reference> opt = registry.getHolder(key);
        return opt.map(h -> (Stream<TagKey<T>>) h.tags()).orElse(Stream.empty());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Either<ResourceKey<T>, T> unwrap() {
        ResourceKey<R> key = getKey();
        if (key != null) {
            return Either.left((ResourceKey<T>) key);
        }
        return Either.right(get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ResourceKey<T>> unwrapKey() {
        ResourceKey<R> key = getKey();
        return key != null ? Optional.of((ResourceKey<T>) key) : Optional.empty();
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return true;
    }
}
