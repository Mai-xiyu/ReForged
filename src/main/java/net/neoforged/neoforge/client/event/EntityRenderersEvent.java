package net.neoforged.neoforge.client.event;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.function.Supplier;

/**
 * NeoForge shim wrapping Forge's EntityRenderersEvent sub-events.
 *
 * <p>Each inner class wraps the corresponding Forge event, delegating method calls.
 * The event bus adapter creates instances of these wrappers when Forge fires its events.</p>
 */
public class EntityRenderersEvent {

    /** Wraps {@code net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers}. */
    public static class RegisterRenderers {
        private final net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers delegate;

        public RegisterRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers delegate) {
            this.delegate = delegate;
        }
    }

    /** Wraps {@code net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static class RegisterLayerDefinitions {
        private final net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions delegate;

        public RegisterLayerDefinitions(net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions delegate) {
            this.delegate = delegate;
        }

        public void registerLayerDefinition(ModelLayerLocation layerLocation, Supplier<LayerDefinition> supplier) {
            delegate.registerLayerDefinition(layerLocation, supplier);
        }
    }
}
