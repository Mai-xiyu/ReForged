package net.neoforged.neoforge.client.event;

import com.google.common.collect.BiMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Stub: Fired to register sprite source types.
 */
public class RegisterSpriteSourceTypesEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final BiMap<ResourceLocation, SpriteSourceType> types;

    public RegisterSpriteSourceTypesEvent(BiMap<ResourceLocation, SpriteSourceType> types) {
        this.types = types;
    }

    @Deprecated(forRemoval = true, since = "1.21.1")
    public SpriteSourceType register(ResourceLocation id, MapCodec<? extends SpriteSource> codec) {
        if (this.types.containsKey(id)) {
            throw new IllegalStateException("Duplicate sprite source type registration " + id);
        }
        SpriteSourceType sourceType = new SpriteSourceType(codec);
        register(id, sourceType);
        return sourceType;
    }

    public void register(ResourceLocation id, SpriteSourceType sourceType) {
        if (this.types.containsKey(id)) {
            throw new IllegalStateException("Duplicate sprite source type registration " + id);
        }
        this.types.put(id, sourceType);
    }
}
