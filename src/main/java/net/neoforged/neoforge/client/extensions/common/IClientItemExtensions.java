package net.neoforged.neoforge.client.extensions.common;

/**
 * Proxy: NeoForge's IClientItemExtensions.
 * <p>
 * In NeoForge this interface lives under {@code net.neoforged.neoforge.client.extensions.common},
 * while in Forge it is under {@code net.minecraftforge.client.extensions.common}.
 * The methods are identical, so we simply extend Forge's version.
 * </p>
 * <p>
 * NeoForge mods that implement this interface (e.g. in {@code Item.initializeClient()})
 * will automatically also implement Forge's interface due to inheritance, so the
 * consumer from Forge's {@code Item.initClient()} can accept the object.
 * </p>
 */
public interface IClientItemExtensions extends net.minecraftforge.client.extensions.common.IClientItemExtensions {

    /**
     * Singleton DEFAULT instance, matching NeoForge's API.
     */
    IClientItemExtensions DEFAULT = new IClientItemExtensions() {};
}
