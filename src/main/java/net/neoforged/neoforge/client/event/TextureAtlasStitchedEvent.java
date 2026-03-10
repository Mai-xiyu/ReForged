package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Stub: Fired after a texture atlas has been stitched.
 */
public class TextureAtlasStitchedEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final TextureAtlas atlas;

    public TextureAtlasStitchedEvent(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public TextureAtlas getAtlas() { return atlas; }
}
