package net.neoforged.neoforge.capabilities;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * NeoForge's ItemCapability — capability lookup for items.
 * <p>Falls back to Forge's capability system on the ItemStack when no provider is found.</p>
 */
public final class ItemCapability<T, C extends @Nullable Object> extends BaseCapability<T, C> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, ItemCapability<?, ?>> registry = new ConcurrentHashMap<>();

    final Map<Item, List<ICapabilityProvider<ItemStack, C, T>>> providers = new IdentityHashMap<>();

    private ItemCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
    }

    @SuppressWarnings("unchecked")
    public static <T, C extends @Nullable Object> ItemCapability<T, C> create(
            ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        return (ItemCapability<T, C>) registry.computeIfAbsent(name,
                n -> new ItemCapability<>(n, typeClass, contextClass));
    }

    @SuppressWarnings("unchecked")
    public static <T> ItemCapability<T, @Nullable Void> createVoid(ResourceLocation name, Class<T> typeClass) {
        return (ItemCapability<T, @Nullable Void>) create(name, typeClass, (Class<@Nullable Void>) Void.class);
    }

    public static synchronized List<ItemCapability<?, ?>> getAll() {
        return new ArrayList<>(registry.values());
    }

    /**
     * Query an item capability.
     * <ol>
     *   <li>First checks registered NeoForge-style providers</li>
     *   <li>Falls back to Forge's capability on ItemStack</li>
     * </ol>
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public T getCapability(ItemStack stack, C context) {
        if (stack.isEmpty()) return null;

        // 1. Try NeoForge-style providers
        for (var provider : providers.getOrDefault(stack.getItem(), List.of())) {
            var ret = provider.getCapability(stack, context);
            if (ret != null) return ret;
        }

        // Note: Forge 1.21 (51.x) removed capabilities from ItemStack,
        // so no Forge capability fallback is available for items.
        return null;
    }
}
