package net.neoforged.neoforge.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Manager for world preset editor screens.
 */
public final class PresetEditorManager {
    private PresetEditorManager() {}

    private static Map<ResourceKey<WorldPreset>, PresetEditor> editors = Map.of();

    /**
     * Initializes preset editors from vanilla's PresetEditor.EDITORS map.
     */
    @ApiStatus.Internal
    @SuppressWarnings("deprecation")
    public static void init() {
        Map<ResourceKey<WorldPreset>, PresetEditor> gatheredEditors = new HashMap<>();
        PresetEditor.EDITORS.forEach((k, v) -> k.ifPresent(key -> gatheredEditors.put(key, v)));
        editors = gatheredEditors;
    }

    /**
     * Gets the preset editor for the given world preset, if registered.
     */
    @Nullable
    public static PresetEditor get(ResourceKey<WorldPreset> key) {
        return editors.get(key);
    }
}
