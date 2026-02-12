package net.neoforged.fml;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Proxy: NeoForge's InterModComms → delegates to Forge's InterModComms.
 */
public final class InterModComms {
    private InterModComms() {}

    public static boolean sendTo(String modId, String method, Supplier<?> thing) {
        return net.minecraftforge.fml.InterModComms.sendTo(modId, method, thing);
    }
}
