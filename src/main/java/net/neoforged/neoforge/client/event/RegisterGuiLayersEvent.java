package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.IModBusEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NeoForge shim for GUI layer registration.
 * <p>Forge 1.21 (51.x) has no {@code AddGuiOverlayLayersEvent} or
 * {@code ForgeLayeredDraw}, so this event is entirely self-contained.
 * Layers registered here are collected and can be applied to the vanilla
 * {@link LayeredDraw} via {@link #applyTo(LayeredDraw)}.</p>
 */
public class RegisterGuiLayersEvent extends net.minecraftforge.eventbus.api.Event implements IModBusEvent {

    /** Ordered map of id → layer, preserving insertion / relative order. */
    private final LinkedHashMap<ResourceLocation, LayeredDraw.Layer> layers = new LinkedHashMap<>();

    public RegisterGuiLayersEvent() {}

    /**
     * Register a GUI layer above all existing layers (rendered on top).
     */
    public void registerAboveAll(ResourceLocation id, LayeredDraw.Layer layer) {
        layers.put(id, layer);
    }

    /**
     * Register a GUI layer below all existing layers (rendered first / underneath).
     */
    public void registerBelowAll(ResourceLocation id, LayeredDraw.Layer layer) {
        // Re-insert at the front of the ordered map
        LinkedHashMap<ResourceLocation, LayeredDraw.Layer> copy = new LinkedHashMap<>();
        copy.put(id, layer);
        copy.putAll(layers);
        layers.clear();
        layers.putAll(copy);
    }

    /**
     * Register a GUI layer above a specific existing layer.
     */
    public void registerAbove(ResourceLocation existingLayer, ResourceLocation id, LayeredDraw.Layer layer) {
        LinkedHashMap<ResourceLocation, LayeredDraw.Layer> copy = new LinkedHashMap<>();
        boolean inserted = false;
        for (Map.Entry<ResourceLocation, LayeredDraw.Layer> entry : layers.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
            if (entry.getKey().equals(existingLayer)) {
                copy.put(id, layer);
                inserted = true;
            }
        }
        if (!inserted) {
            copy.put(id, layer); // fallback: append
        }
        layers.clear();
        layers.putAll(copy);
    }

    /**
     * Register a GUI layer below a specific existing layer.
     */
    public void registerBelow(ResourceLocation existingLayer, ResourceLocation id, LayeredDraw.Layer layer) {
        LinkedHashMap<ResourceLocation, LayeredDraw.Layer> copy = new LinkedHashMap<>();
        boolean inserted = false;
        for (Map.Entry<ResourceLocation, LayeredDraw.Layer> entry : layers.entrySet()) {
            if (entry.getKey().equals(existingLayer) && !inserted) {
                copy.put(id, layer);
                inserted = true;
            }
            copy.put(entry.getKey(), entry.getValue());
        }
        if (!inserted) {
            copy.put(id, layer); // fallback: append
        }
        layers.clear();
        layers.putAll(copy);
    }

    /**
     * Returns all registered layers in order.
     */
    public List<LayeredDraw.Layer> getOrderedLayers() {
        return new ArrayList<>(layers.values());
    }

    /**
     * Apply all registered layers to the given vanilla {@link LayeredDraw}.
     */
    public void applyTo(LayeredDraw draw) {
        for (LayeredDraw.Layer layer : layers.values()) {
            draw.add(layer);
        }
    }
}
