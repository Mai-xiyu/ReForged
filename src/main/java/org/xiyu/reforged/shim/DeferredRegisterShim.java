package org.xiyu.reforged.shim;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * DeferredRegisterShim — Bridges NeoForge's {@code DeferredRegister} API to Forge's.
 *
 * <h3>Key differences between NeoForge and Forge DeferredRegister:</h3>
 * <ul>
 *     <li>NeoForge uses {@code DeferredRegister.create(ResourceKey, String)} —
 *         Forge uses {@code DeferredRegister.create(IForgeRegistry, String)}</li>
 *     <li>NeoForge supports parallel registration — Forge does serial registration</li>
 *     <li>NeoForge returns {@code DeferredHolder<T>} — Forge returns {@code RegistryObject<T>}</li>
 * </ul>
 *
 * <p>This shim wraps Forge's {@code DeferredRegister} to accept the NeoForge-style
 * factory methods and return compatible types.</p>
 *
 * @param <T> the registry entry type
 */
public final class DeferredRegisterShim<T> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final DeferredRegister<T> forgeRegister;
    private final String modId;
    private final String registryName;

    private DeferredRegisterShim(DeferredRegister<T> forgeRegister, String modId, String registryName) {
        this.forgeRegister = forgeRegister;
        this.modId = modId;
        this.registryName = registryName;
    }

    // ─── Factory methods (matching NeoForge's API) ─────────────────

    /**
     * NeoForge-style factory: {@code DeferredRegister.create(String registryName, String modId)}.
     *
     * <p>We map the registry name to Forge's {@code IForgeRegistry} if possible,
     * falling back to a vanilla {@code ResourceKey}-based register.
     */
    @SuppressWarnings("unchecked")
    public static <T> DeferredRegisterShim<T> create(String registryName, String modId) {
        LOGGER.debug("[ReForged] DeferredRegisterShim.create({}, {})", registryName, modId);

        IForgeRegistry<T> forgeRegistry = findForgeRegistry(registryName);
        DeferredRegister<T> forge;
        if (forgeRegistry != null) {
            forge = DeferredRegister.create(forgeRegistry, modId);
        } else {
            // Fall back: try to create from ResourceKey
            ResourceKey<Registry<T>> key = (ResourceKey<Registry<T>>) (ResourceKey<?>)
                    ResourceKey.createRegistryKey(ResourceLocation.parse(registryName));
            forge = DeferredRegister.create(key, modId);
        }

        return new DeferredRegisterShim<>(forge, modId, registryName);
    }

    /**
     * NeoForge-style factory using a {@code ResourceKey<Registry<T>>}.
     */
    public static <T> DeferredRegisterShim<T> create(ResourceKey<Registry<T>> registryKey, String modId) {
        LOGGER.debug("[ReForged] DeferredRegisterShim.create(ResourceKey:{}, {})", registryKey.location(), modId);
        DeferredRegister<T> forge = DeferredRegister.create(registryKey, modId);
        return new DeferredRegisterShim<>(forge, modId, registryKey.location().toString());
    }

    // ─── Registration methods ──────────────────────────────────────

    /**
     * Register a new entry. Returns a {@code RegistryObject<T>} (compatible with
     * NeoForge's {@code DeferredHolder<T>} after bytecode rewriting).
     */
    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
        LOGGER.debug("[ReForged] DeferredRegisterShim: registering '{}:{}' in {}", modId, name, registryName);
        return forgeRegister.register(name, supplier);
    }

    /**
     * Hook this deferred register to the mod event bus.
     * Must be called during mod construction.
     */
    public void register(IEventBus modEventBus) {
        LOGGER.debug("[ReForged] DeferredRegisterShim: attaching to mod event bus for registry '{}'", registryName);
        forgeRegister.register(modEventBus);
    }

    /**
     * Get the underlying Forge DeferredRegister (for advanced use).
     */
    public DeferredRegister<T> getForgeRegister() {
        return forgeRegister;
    }

    // ─── Internal helpers ──────────────────────────────────────────

    /**
     * Attempt to find a Forge IForgeRegistry by name.
     * Maps common NeoForge registry paths to their Forge equivalents.
     */
    @SuppressWarnings("unchecked")
    private static <T> IForgeRegistry<T> findForgeRegistry(String registryName) {
        // NeoForge and Forge share many registry paths under "minecraft:" namespace
        return switch (registryName) {
            case "minecraft:block",  "block"  -> (IForgeRegistry<T>) ForgeRegistries.BLOCKS;
            case "minecraft:item",   "item"   -> (IForgeRegistry<T>) ForgeRegistries.ITEMS;
            case "minecraft:entity_type", "entity_type" -> (IForgeRegistry<T>) ForgeRegistries.ENTITY_TYPES;
            case "minecraft:block_entity_type", "block_entity_type" -> (IForgeRegistry<T>) ForgeRegistries.BLOCK_ENTITY_TYPES;
            case "minecraft:menu",   "menu"   -> (IForgeRegistry<T>) ForgeRegistries.MENU_TYPES;
            case "minecraft:sound_event", "sound_event" -> (IForgeRegistry<T>) ForgeRegistries.SOUND_EVENTS;
            case "minecraft:potion", "potion" -> (IForgeRegistry<T>) ForgeRegistries.POTIONS;
            // Note: enchantments are data-driven in 1.21.1, not in ForgeRegistries — handled by ResourceKey fallback
            default -> {
                LOGGER.warn("[ReForged] Unknown registry '{}', will attempt ResourceKey fallback", registryName);
                yield null;
            }
        };
    }
}
