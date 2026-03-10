package net.neoforged.neoforge.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.world.item.component.FireworkExplosion;
import org.jetbrains.annotations.Nullable;

/**
 * Registry for custom firework shape rendering factories.
 */
public class FireworkShapeFactoryRegistry {
    private static final Map<FireworkExplosion.Shape, Factory> factories = new HashMap<>();

    public interface Factory {
        void build(FireworkParticles.Starter starter, boolean trail, boolean flicker, int[] colors, int[] fadeColors);
    }

    public static void register(FireworkExplosion.Shape shape, Factory factory) {
        factories.put(shape, factory);
    }

    @Nullable
    public static Factory get(FireworkExplosion.Shape shape) {
        return factories.get(shape);
    }
}
