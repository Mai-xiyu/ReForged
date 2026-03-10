package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraftforge.eventbus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired to allow registration of custom world preset editors.
 * This event is fired on the mod-specific event bus.
 */
public class RegisterPresetEditorsEvent extends Event implements IModBusEvent {
    private final Map<ResourceKey<WorldPreset>, PresetEditor> editors;

    public RegisterPresetEditorsEvent(Map<ResourceKey<WorldPreset>, PresetEditor> editors) {
        this.editors = editors;
    }

    public void register(ResourceKey<WorldPreset> key, PresetEditor editor) {
        editors.put(key, editor);
    }
}
