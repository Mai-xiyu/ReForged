package net.neoforged.neoforge.registries;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Proxy for NeoForge's {@code DeferredRegister}.
 * Uses composition to wrap Forge's DeferredRegister, since constructor signatures differ.
 *
 * <p>NeoForge mods use {@code DeferredRegister.create(Registries.BLOCKS, MODID)};
 * we translate to Forge's static factory methods.</p>
 *
 * <h3>No-op mode</h3>
 * <p>For NeoForge-only registries that have no Forge equivalent
 * (e.g. {@code neoforge:attachment_types}), the register uses <em>no-op mode</em>:
 * entries are stored locally and their suppliers are evaluated lazily.
 * {@link #register(IEventBus)} becomes a no-op so Forge never tries to look up the
 * non-existent registry.</p>
 */
public class DeferredRegister<T> {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Known registry namespaces that exist in Forge and can be safely delegated. */
    private static final Set<String> FORGE_KNOWN_NAMESPACES = Set.of("minecraft", "forge");

    /** NeoForge-only registry paths that explicitly have no Forge equivalent. */
    private static final Set<String> NEOFORGE_ONLY_REGISTRIES = Set.of(
            "neoforge:attachment_types"
    );

    protected final net.minecraftforge.registries.DeferredRegister<T> delegate; // null in no-op mode
    private final String modid;
    private final ResourceKey<? extends Registry<T>> registryKey; // stored for key generation
    protected boolean isNoOp;
    /** True when this DeferredRegister owns a custom registry built via makeRegistry(). */
    private boolean customRegistry;
    /** The custom MappedRegistry created by makeRegistry(), null otherwise. */
    private Registry<T> customRegistryInstance;

    protected DeferredRegister(net.minecraftforge.registries.DeferredRegister<T> delegate,
                               String modid, boolean isNoOp, ResourceKey<? extends Registry<T>> registryKey) {
        this.delegate = delegate;
        this.modid = modid;
        this.isNoOp = isNoOp;
        this.registryKey = registryKey;
    }

    /* ---------- Namespace remapping ---------- */

    /**
     * Remap NeoForge-specific registry namespaces to Forge equivalents.
     * e.g. {@code neoforge:global_loot_modifier_serializers} → {@code forge:global_loot_modifier_serializers}
     */
    private static ResourceLocation remapRegistryName(ResourceLocation name) {
        if ("neoforge".equals(name.getNamespace())) {
            return ResourceLocation.fromNamespaceAndPath("forge", name.getPath());
        }
        return name;
    }

    /**
     * Check if a registry name (after remapping) should use no-op mode.
     * Returns true for mod-defined custom registries and NeoForge-only registries.
     */
    private static boolean shouldBeNoOp(ResourceLocation originalName, ResourceLocation remappedName) {
        // Explicit NeoForge-only registries
        if (NEOFORGE_ONLY_REGISTRIES.contains(originalName.toString())) {
            return true;
        }
        // After remapping, only minecraft: and forge: registries exist in Forge.
        // Everything else (mod custom registries like champions:*, etc.) → no-op
        return !FORGE_KNOWN_NAMESPACES.contains(remappedName.getNamespace());
    }

    /* ---------- Factory methods ---------- */

    /**
     * NeoForge-style factory with ResourceKey.
     */
    @SuppressWarnings("unchecked")
    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        ResourceLocation registryName = registryKey.location();
        ResourceLocation remapped = remapRegistryName(registryName);

        if (shouldBeNoOp(registryName, remapped)) {
            LOGGER.info("[ReForged] Registry '{}' has no Forge equivalent. Using no-op DeferredRegister for mod '{}'",
                    registryName, modid);
            return new DeferredRegister<>(null, modid, true, registryKey);
        }

        net.minecraftforge.registries.DeferredRegister<T> forgeReg =
                net.minecraftforge.registries.DeferredRegister.create(remapped, modid);
        return new DeferredRegister<>(forgeReg, modid, false, registryKey);
    }

    /**
     * NeoForge-style factory with IForgeRegistry.
     */
    public static <B> DeferredRegister<B> create(IForgeRegistry<B> registry, String modid) {
        net.minecraftforge.registries.DeferredRegister<B> forgeReg =
                net.minecraftforge.registries.DeferredRegister.create(registry, modid);
        return new DeferredRegister<>(forgeReg, modid, false, null);
    }

    /**
     * NeoForge-style factory with vanilla Registry object.
     */
    public static <B> DeferredRegister<B> create(Registry<B> registry, String modid) {
        return create(registry.key(), modid);
    }

    /**
     * NeoForge convenience factory with ResourceLocation.
     */
    @SuppressWarnings("unchecked")
    public static <T> DeferredRegister<T> create(ResourceLocation registryName, String modid) {
        ResourceLocation remapped = remapRegistryName(registryName);

        // Construct a registry key for tracking
        ResourceKey<Registry<T>> rk = (ResourceKey<Registry<T>>) (ResourceKey<?>) ResourceKey.createRegistryKey(registryName);

        if (shouldBeNoOp(registryName, remapped)) {
            LOGGER.info("[ReForged] Registry '{}' has no Forge equivalent. Using no-op DeferredRegister for mod '{}'",
                    registryName, modid);
            return new DeferredRegister<>(null, modid, true, rk);
        }

        net.minecraftforge.registries.DeferredRegister<T> forgeReg =
                net.minecraftforge.registries.DeferredRegister.create(remapped, modid);
        return new DeferredRegister<>(forgeReg, modid, false, rk);
    }

    /* ---------- Registration ---------- */

    /**
     * Register an entry — returns a DeferredHolder (NeoForge's RegistryObject).
     * In no-op mode, returns a direct holder backed by the supplier.
     * The supplier is also stored as a fallback for late registration support.
     */
    @SuppressWarnings("unchecked")
    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> sup) {
        if (customRegistry && customRegistryInstance != null) {
            // Custom registry mode: register directly into the MappedRegistry
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modid, name);
            ResourceKey<T> entryKey = ResourceKey.create((ResourceKey<Registry<T>>) registryKey, id);
            try {
                // Unfreeze if needed, register, then optionally re-freeze
                unfreezeRegistry(customRegistryInstance);
                I value = sup.get();
                Registry.register((Registry<T>) customRegistryInstance, id, value);
                LOGGER.info("[ReForged] Registered '{}' into custom registry '{}'", id, registryKey.location());
                return (DeferredHolder<T, I>) DeferredHolder.createDirect(entryKey, () -> value);
            } catch (Throwable t) {
                LOGGER.error("[ReForged] Failed to register '{}' into custom registry '{}': {}",
                        id, registryKey.location(), t.getMessage());
                return (DeferredHolder<T, I>) DeferredHolder.createDirect(entryKey, sup);
            }
        }
        if (isNoOp) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modid, name);
            LOGGER.debug("[ReForged] No-op register: {}", id);
            if (registryKey != null) {
                // Create a full ResourceKey for this entry so getKey() works downstream (Registrate)
                ResourceKey<T> entryKey = ResourceKey.create((ResourceKey<Registry<T>>) registryKey, id);
                return (DeferredHolder<T, I>) DeferredHolder.createDirect(entryKey, sup);
            }
            return DeferredHolder.createDirect(id, sup);
        }
        LOGGER.info("[ReForged] Registering entry '{}'  for mod '{}' via Forge DeferredRegister", name, modid);
        RegistryObject<I> obj = delegate.register(name, sup);
        LOGGER.info("[ReForged] RegistryObject created: {} (id={})", obj, obj.getId());
        return DeferredHolder.wrap(obj, sup);
    }

    /**
     * Register an entry using a Function that receives the ResourceLocation key.
     * NeoForge API — used by mods like Balm that call register(String, Function).
     * Constructs the ResourceLocation and delegates to the Supplier variant.
     */
    public <I extends T> DeferredHolder<T, I> register(String name, java.util.function.Function<ResourceLocation, ? extends I> func) {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(modid, name);
        return this.register(name, () -> func.apply(key));
    }

    /**
     * Register this DeferredRegister to the given event bus.
     * In no-op mode, this is a safe no-op.
     * In custom registry mode, this is also a no-op since entries are registered directly.
     */
    public void register(IEventBus bus) {
        if (customRegistry) {
            LOGGER.debug("[ReForged] Skipping DeferredRegister.register(bus) for custom registry mod '{}'", modid);
            return;
        }
        if (isNoOp) {
            LOGGER.debug("[ReForged] Skipping no-op DeferredRegister.register() for mod '{}'", modid);
            return;
        }
        LOGGER.info("[ReForged] DeferredRegister.register() for mod '{}', delegate entries: {}, delegate class: {}",
                modid, delegate.getEntries().size(), delegate.getClass().getName());
        delegate.register(bus);
        LOGGER.info("[ReForged] DeferredRegister.register() completed for mod '{}'", modid);
    }

    /**
     * Get all registered entries.
     */
    public Collection<RegistryObject<T>> getEntries() {
        if (isNoOp) return Collections.emptyList();
        return delegate.getEntries();
    }

    /**
     * Get the namespace of this registry.
     */
    public String getNamespace() {
        return this.modid;
    }

    /**
     * Adds an alias that maps from one registry name to another.
     * In Forge's DeferredRegister, aliases are not directly supported, so this is a no-op.
     *
     * @param from The source registry name to alias from.
     * @param to   The target registry name to alias to.
     */
    public void addAlias(net.minecraft.resources.ResourceLocation from, net.minecraft.resources.ResourceLocation to) {
        LOGGER.debug("[ReForged] DeferredRegister.addAlias({} -> {}) for mod '{}' — no-op in Forge shim", from, to, modid);
    }

    /**
     * NeoForge's makeRegistry — creates a custom registry via a RegistryBuilder.
     * Returns a Supplier that yields the newly created registry.
     * <p>
     * Since mod-defined custom registries don't exist in Forge, calling this
     * also switches this DeferredRegister to no-op mode.
     * </p>
     */
    @SuppressWarnings("unchecked")
    public Supplier<Registry<T>> makeRegistry(Supplier<RegistryBuilder<T>> builderSup) {
        LOGGER.info("[ReForged] makeRegistry() called for mod '{}' — using custom registry mode", modid);
        this.customRegistry = true;
        this.isNoOp = false;
        RegistryBuilder<T> builder = builderSup.get();
        Registry<T> registry = builder.build();
        this.customRegistryInstance = registry;
        return () -> registry;
    }

    /**
     * NeoForge's makeRegistry — creates a custom registry via a Consumer-based RegistryBuilder.
     * Returns the newly created Registry.
     * <p>
     * Since mod-defined custom registries don't exist in Forge, calling this
     * also switches this DeferredRegister to no-op mode.
     * </p>
     */
    @SuppressWarnings("unchecked")
    public Registry<T> makeRegistry(Consumer<RegistryBuilder<T>> consumer) {
        LOGGER.info("[ReForged] makeRegistry(Consumer) called for mod '{}' — using custom registry mode", modid);
        this.customRegistry = true;
        this.isNoOp = false;
        RegistryBuilder<T> builder = new RegistryBuilder<>(registryKey);
        consumer.accept(builder);
        Registry<T> registry = builder.build();
        this.customRegistryInstance = registry;
        return registry;
    }

    /**
     * Unfreeze a MappedRegistry so entries can be registered into it.
     * The registry may have been frozen by RegistryBuilder.build().
     */
    private static void unfreezeRegistry(Registry<?> registry) {
        try {
            java.lang.reflect.Field frozenField = null;
            Class<?> clazz = registry.getClass();
            while (clazz != null && clazz != Object.class) {
                for (String name : new String[]{"frozen", "f_205845_"}) {
                    try {
                        frozenField = clazz.getDeclaredField(name);
                        break;
                    } catch (NoSuchFieldException ignored) {}
                }
                if (frozenField != null) break;
                clazz = clazz.getSuperclass();
            }
            if (frozenField != null) {
                frozenField.setAccessible(true);
                frozenField.setBoolean(registry, false);
            }
        } catch (Throwable t) {
            LOGGER.debug("[ReForged] Could not unfreeze registry: {}", t.getMessage());
        }
    }

    /* ---------- Tag key creation ---------- */

    /**
     * Creates a tag key based on the current namespace and provided path as the location
     * and the registry name linked to this DeferredRegister.
     *
     * @see #createTagKey(ResourceLocation)
     */
    @SuppressWarnings("unchecked")
    public TagKey<T> createTagKey(String path) {
        return createTagKey(ResourceLocation.fromNamespaceAndPath(this.modid, path));
    }

    /**
     * Creates a tag key based on the provided resource location and the registry name
     * linked to this DeferredRegister.
     *
     * @see #createTagKey(String)
     */
    @SuppressWarnings("unchecked")
    public TagKey<T> createTagKey(ResourceLocation location) {
        if (registryKey == null) {
            throw new IllegalStateException("Cannot create a TagKey without a registry key");
        }
        return TagKey.create(registryKey, location);
    }

    /* ---------- Registry supplier ---------- */

    /**
     * Returns a supplier for the Registry linked to this DeferredRegister.
     * For vanilla registries this returns the built-in registry; for custom registries
     * it returns null until makeRegistry is called.
     */
    @SuppressWarnings("unchecked")
    public Supplier<Registry<T>> getRegistry() {
        return () -> {
            if (registryKey == null) return null;
            return (Registry<T>) net.minecraft.core.registries.BuiltInRegistries.REGISTRY.get(registryKey.location());
        };
    }

    /* ---------- DataComponents specialisation ---------- */

    /**
     * NeoForge's DataComponents specialization of DeferredRegister.
     * Provides {@code registerComponentType()} for NeoForge mods that register
     * custom data component types via a builder pattern.
     */
    public static class DataComponents extends DeferredRegister<net.minecraft.core.component.DataComponentType<?>> {
        @SuppressWarnings("unchecked")
        protected DataComponents(String modid) {
            super(net.minecraftforge.registries.DeferredRegister.create(
                    net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, modid), modid, false,
                    (ResourceKey<? extends Registry<net.minecraft.core.component.DataComponentType<?>>>) (ResourceKey<?>) net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE);
        }

        public static DataComponents createDataComponents(String modid) {
            return new DataComponents(modid);
        }

        /**
         * Register a new {@link net.minecraft.core.component.DataComponentType} using a builder.
         *
         * <p>NeoForge mods call this as:
         * <pre>{@code
         * DATA_COMPONENTS.registerComponentType("my_component",
         *     builder -> builder.persistent(MyCodec.CODEC).networkSynchronized(MyCodec.STREAM_CODEC));
         * }</pre>
         *
         * @param name            the registry name
         * @param builderOperator configures the {@code DataComponentType.Builder}
         * @return a DeferredHolder for the registered component type
         */
        @SuppressWarnings("unchecked")
        public <T> DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<T>>
        registerComponentType(String name, java.util.function.UnaryOperator<net.minecraft.core.component.DataComponentType.Builder<T>> builderOperator) {
            return (DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<T>>)
                    (DeferredHolder<?, ?>) this.register(name, () -> {
                        net.minecraft.core.component.DataComponentType.Builder<T> builder =
                                net.minecraft.core.component.DataComponentType.builder();
                        builder = builderOperator.apply(builder);
                        return builder.build();
                    });
        }
    }

    /**
     * NeoForge factory for DataComponents.
     */
    public static DataComponents createDataComponents(
            ResourceKey<? extends Registry<net.minecraft.core.component.DataComponentType<?>>> registryKey,
            String modid) {
        return new DataComponents(modid);
    }

    /* ---------- Items specialisation ---------- */

    /**
     * NeoForge's Items specialization of DeferredRegister.
     * Provides {@code registerSimpleItem()}, {@code registerItem()}, and
     * {@code registerSimpleBlockItem()} for NeoForge mods that register items.
     */
    public static class Items extends DeferredRegister<net.minecraft.world.item.Item> {

        protected Items(String modid) {
            super(net.minecraftforge.registries.DeferredRegister.create(
                    net.minecraft.core.registries.Registries.ITEM, modid), modid, false,
                    net.minecraft.core.registries.Registries.ITEM);
        }

        /**
         * Factory method matching NeoForge's {@code DeferredRegister.createItems(String)}.
         */
        public static Items createItems(String modid) {
            return new Items(modid);
        }

        /**
         * Override register to return DeferredItem instead of DeferredHolder.
         * This is required because NeoForge mods expect Items.register() to return DeferredItem.
         */
        @Override
        @SuppressWarnings("unchecked")
        public <I extends net.minecraft.world.item.Item> DeferredItem<I> register(String name, Supplier<? extends I> sup) {
            if (isNoOp) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(getNamespace(), name);
                return DeferredItem.createDirectItem(id, sup);
            }
            RegistryObject<I> obj = delegate.register(name, sup);
            return DeferredItem.wrapItem(obj, sup);
        }

        /**
         * Override register(Function) to return DeferredItem.
         */
        @Override
        @SuppressWarnings("unchecked")
        public <I extends net.minecraft.world.item.Item> DeferredItem<I> register(String name, java.util.function.Function<ResourceLocation, ? extends I> func) {
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(getNamespace(), name);
            return this.register(name, () -> func.apply(key));
        }

        /**
         * Register a simple item with default properties.
         */
        public DeferredItem<net.minecraft.world.item.Item>
        registerSimpleItem(String name) {
            return registerItem(name, net.minecraft.world.item.Item::new, new net.minecraft.world.item.Item.Properties());
        }

        /**
         * Register a simple item with specified properties.
         */
        public DeferredItem<net.minecraft.world.item.Item>
        registerSimpleItem(String name, net.minecraft.world.item.Item.Properties properties) {
            return registerItem(name, net.minecraft.world.item.Item::new, properties);
        }

        /**
         * Register an item using a factory function and default properties.
         */
        public <I extends net.minecraft.world.item.Item> DeferredItem<I>
        registerItem(String name, java.util.function.Function<net.minecraft.world.item.Item.Properties, ? extends I> factory) {
            return registerItem(name, factory, new net.minecraft.world.item.Item.Properties());
        }

        /**
         * Register an item using a factory function and specified properties.
         */
        public <I extends net.minecraft.world.item.Item> DeferredItem<I>
        registerItem(String name, java.util.function.Function<net.minecraft.world.item.Item.Properties, ? extends I> factory,
                     net.minecraft.world.item.Item.Properties properties) {
            return this.register(name, () -> factory.apply(properties));
        }

        /**
         * Register a simple BlockItem by name with a block Supplier and properties.
         */
        public DeferredItem<net.minecraft.world.item.BlockItem>
        registerSimpleBlockItem(String name, Supplier<? extends net.minecraft.world.level.block.Block> block, net.minecraft.world.item.Item.Properties properties) {
            return this.register(name, key -> new net.minecraft.world.item.BlockItem(block.get(), properties));
        }

        /**
         * Register a simple BlockItem by name with a block Supplier and default properties.
         */
        public DeferredItem<net.minecraft.world.item.BlockItem>
        registerSimpleBlockItem(String name, Supplier<? extends net.minecraft.world.level.block.Block> block) {
            return this.registerSimpleBlockItem(name, block, new net.minecraft.world.item.Item.Properties());
        }

        /**
         * Register a simple BlockItem for a given Holder<Block> with properties.
         * Name is derived from the block's registry key.
         */
        public DeferredItem<net.minecraft.world.item.BlockItem>
        registerSimpleBlockItem(net.minecraft.core.Holder<net.minecraft.world.level.block.Block> block, net.minecraft.world.item.Item.Properties properties) {
            return this.registerSimpleBlockItem(block.unwrapKey().orElseThrow().location().getPath(), block::value, properties);
        }

        /**
         * Register a simple BlockItem for a given Holder<Block> with default properties.
         * Name is derived from the block's registry key.
         */
        public DeferredItem<net.minecraft.world.item.BlockItem>
        registerSimpleBlockItem(net.minecraft.core.Holder<net.minecraft.world.level.block.Block> block) {
            return this.registerSimpleBlockItem(block, new net.minecraft.world.item.Item.Properties());
        }

        /**
         * Register a simple BlockItem for a given block instance.
         */
        public DeferredItem<net.minecraft.world.item.BlockItem>
        registerSimpleBlockItem(net.minecraft.world.level.block.Block block) {
            return registerSimpleBlockItem(block, new net.minecraft.world.item.Item.Properties());
        }

        /**
         * Register a simple BlockItem for a given block instance with given properties.
         */
        public DeferredItem<net.minecraft.world.item.BlockItem>
        registerSimpleBlockItem(net.minecraft.world.level.block.Block block, net.minecraft.world.item.Item.Properties properties) {
            return this.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath(),
                    () -> new net.minecraft.world.item.BlockItem(block, properties));
        }

        /**
         * Register a simple BlockItem using a DeferredHolder for the block.
         */
        public DeferredItem<net.minecraft.world.item.BlockItem>
        registerSimpleBlockItem(DeferredHolder<net.minecraft.world.level.block.Block, ? extends net.minecraft.world.level.block.Block> blockHolder) {
            return this.register(blockHolder.getId().getPath(),
                    () -> new net.minecraft.world.item.BlockItem(blockHolder.get(), new net.minecraft.world.item.Item.Properties()));
        }

        /**
         * Register a simple BlockItem using a DeferredHolder for the block with given properties.
         */
        public DeferredItem<net.minecraft.world.item.BlockItem>
        registerSimpleBlockItem(DeferredHolder<net.minecraft.world.level.block.Block, ? extends net.minecraft.world.level.block.Block> blockHolder,
                                net.minecraft.world.item.Item.Properties properties) {
            return this.register(blockHolder.getId().getPath(),
                    () -> new net.minecraft.world.item.BlockItem(blockHolder.get(), properties));
        }
    }

    /**
     * NeoForge factory for Items.
     */
    public static Items createItems(String modid) {
        return new Items(modid);
    }

    /* ---------- Blocks specialisation ---------- */

    /**
     * NeoForge's Blocks specialization of DeferredRegister.
     * Provides {@code registerBlock()}, {@code registerSimpleBlock()}, and
     * returns {@link DeferredBlock} from {@code register()}.
     */
    public static class Blocks extends DeferredRegister<net.minecraft.world.level.block.Block> {

        protected Blocks(String modid) {
            super(net.minecraftforge.registries.DeferredRegister.create(
                    net.minecraft.core.registries.Registries.BLOCK, modid), modid, false,
                    net.minecraft.core.registries.Registries.BLOCK);
        }

        /**
         * Factory method matching NeoForge's {@code DeferredRegister.createBlocks(String)}.
         */
        public static Blocks createBlocks(String modid) {
            return new Blocks(modid);
        }

        /**
         * Override register to return DeferredBlock instead of DeferredHolder.
         */
        @Override
        @SuppressWarnings("unchecked")
        public <I extends net.minecraft.world.level.block.Block> DeferredBlock<I> register(String name, Supplier<? extends I> sup) {
            if (isNoOp) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(getNamespace(), name);
                return new DeferredBlock<>(id, sup);
            }
            RegistryObject<I> obj = delegate.register(name, sup);
            return new DeferredBlock<>(obj, sup);
        }

        /**
         * Override register(Function) to return DeferredBlock.
         */
        @Override
        @SuppressWarnings("unchecked")
        public <I extends net.minecraft.world.level.block.Block> DeferredBlock<I> register(String name, java.util.function.Function<ResourceLocation, ? extends I> func) {
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(getNamespace(), name);
            return this.register(name, () -> func.apply(key));
        }

        /**
         * Register a block using a factory function and specified properties.
         */
        @SuppressWarnings("unchecked")
        public <B extends net.minecraft.world.level.block.Block> DeferredBlock<B>
        registerBlock(String name,
                      java.util.function.Function<net.minecraft.world.level.block.state.BlockBehaviour.Properties, ? extends B> func,
                      net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
            return (DeferredBlock<B>) (DeferredBlock<?>) this.register(name, () -> func.apply(props));
        }

        /**
         * Register a block using a factory function and default properties.
         */
        public <B extends net.minecraft.world.level.block.Block> DeferredBlock<B>
        registerBlock(String name,
                      java.util.function.Function<net.minecraft.world.level.block.state.BlockBehaviour.Properties, ? extends B> func) {
            return this.registerBlock(name, func, net.minecraft.world.level.block.state.BlockBehaviour.Properties.of());
        }

        /**
         * Register a simple block with specified properties.
         */
        public DeferredBlock<net.minecraft.world.level.block.Block>
        registerSimpleBlock(String name, net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
            return this.registerBlock(name, net.minecraft.world.level.block.Block::new, props);
        }

        /**
         * Register a simple block with default properties.
         */
        public DeferredBlock<net.minecraft.world.level.block.Block>
        registerSimpleBlock(String name) {
            return this.registerSimpleBlock(name, net.minecraft.world.level.block.state.BlockBehaviour.Properties.of());
        }
    }

    /**
     * NeoForge factory for Blocks.
     */
    public static Blocks createBlocks(String modid) {
        return new Blocks(modid);
    }
}
