package net.neoforged.fml.loading;

import net.neoforged.api.distmarker.Dist;

/**
 * Proxy: NeoForge's FMLEnvironment.
 * <p>
 * Delegates to Forge's {@link net.minecraftforge.fml.loading.FMLEnvironment}
 * and maps the {@code dist} field from Forge's Dist enum to NeoForge's Dist enum.
 * </p>
 */
public class FMLEnvironment {

    /**
     * The current distribution (CLIENT or DEDICATED_SERVER).
     */
    public static final Dist dist = mapDist(net.minecraftforge.fml.loading.FMLEnvironment.dist);

    /**
     * Whether we are running in a production (non-dev) environment.
     */
    public static final boolean production = net.minecraftforge.fml.loading.FMLEnvironment.production;

    private static Dist mapDist(net.minecraftforge.api.distmarker.Dist forgeDist) {
        return switch (forgeDist) {
            case CLIENT -> Dist.CLIENT;
            case DEDICATED_SERVER -> Dist.DEDICATED_SERVER;
        };
    }
}
