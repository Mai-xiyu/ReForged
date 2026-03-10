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

    /** Wrapper constructor for EventBusAdapter bridging (maps from Forge TextureStitchEvent.Post). */
    public TextureAtlasStitchedEvent(net.minecraftforge.client.event.TextureStitchEvent.Post forge) {
        this(forge.getAtlas());
    }

    /** Wrapper constructor for EventBusAdapter bridging (maps from Forge TextureStitchEvent). */
    public TextureAtlasStitchedEvent(net.minecraftforge.client.event.TextureStitchEvent forge) {
        this(forge.getAtlas());
    }

    public TextureAtlas getAtlas() { return atlas; }
}
