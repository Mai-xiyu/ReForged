package net.neoforged.neoforge.registries;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Lifecycle;

/**
 * Proxy: NeoForge's NeoForgeRegistries — registry keys for NeoForge-specific registries.
 * <p>
 * Provides {@link #ATTACHMENT_TYPES} and {@link Keys} so that NeoForge mods can
 * reference these registries without crashing. The actual registries are lightweight
 * {@link MappedRegistry} stubs; entries registered via {@link DeferredRegister}
 * will be handled in no-op mode.
 * </p>
 */
public final class NeoForgeRegistries {
    private NeoForgeRegistries() {}

    /* ---------- Registry instances ---------- */

    public static final Registry<Object> ATTACHMENT_TYPES = new StubRegistry<>(
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath("neoforge", "attachment_types"))
    );

    public static final Registry<Object> GLOBAL_LOOT_MODIFIER_SERIALIZERS = new StubRegistry<>(
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath("neoforge", "global_loot_modifier_serializers"))
    );

    /* ---------- Registry keys ---------- */

    public static final class Keys {
        private Keys() {}

        public static final ResourceKey<Registry<Object>> ATTACHMENT_TYPES =
                ResourceKey.createRegistryKey(
                        ResourceLocation.fromNamespaceAndPath("neoforge", "attachment_types"));

        public static final ResourceKey<Registry<Object>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
                ResourceKey.createRegistryKey(
                        ResourceLocation.fromNamespaceAndPath("neoforge", "global_loot_modifier_serializers"));

        @SuppressWarnings("unchecked")
        public static final ResourceKey<Registry<Object>> CONDITION_CODECS =
                (ResourceKey<Registry<Object>>) (ResourceKey<?>) ResourceKey.createRegistryKey(
                        ResourceLocation.fromNamespaceAndPath("neoforge", "condition_codecs"));
    }

    /* ---------- Internal stub registry ---------- */

    private static class StubRegistry<T> extends MappedRegistry<T> {
        private final ResourceKey<Registry<T>> key;

        StubRegistry(ResourceKey<Registry<T>> key) {
            super(key, Lifecycle.stable());
            this.key = key;
        }

        @Override
        public ResourceKey<? extends Registry<T>> key() {
            return key;
        }
    }
}
