package net.neoforged.neoforge.client;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ColorResolver;

/**
 * NeoForge-compatible manager for custom `ColorResolver` instances.
 */
public final class ColorResolverManager {
    private static ImmutableList<ColorResolver> colorResolvers = ImmutableList.of();

    private ColorResolverManager() {}

    public static ImmutableList<ColorResolver> getRegisteredResolvers() {
        return colorResolvers;
    }

    public static void registerBlockTintCaches(ClientLevel level, Map<ColorResolver, BlockTintCache> target) {
        for (var resolver : colorResolvers) {
            target.put(resolver, new BlockTintCache(pos -> level.calculateBlockTint(pos, resolver)));
        }
    }

    static void setRegisteredResolvers(ImmutableList<ColorResolver> resolvers) {
        colorResolvers = resolvers;
    }
}
