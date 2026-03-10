package net.neoforged.neoforge.client.gui.map;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Allows users to register custom {@linkplain IMapDecorationRenderer decoration renderers}
 * for map decorations which require more dynamic rendering than a single texture allows.
 *
 * <p>This event is fired on the mod-specific event bus, only on the logical client.</p>
 */
public final class RegisterMapDecorationRenderersEvent extends Event implements IModBusEvent {
    private final Map<MapDecorationType, IMapDecorationRenderer> renderers;

    public RegisterMapDecorationRenderersEvent(Map<MapDecorationType, IMapDecorationRenderer> renderers) {
        this.renderers = renderers;
    }

    public RegisterMapDecorationRenderersEvent() {
        this(new HashMap<>());
    }

    /**
     * Registers a decoration renderer for the given decoration type.
     *
     * @param type     The {@link MapDecorationType} the renderer is used for
     * @param renderer The {@link IMapDecorationRenderer} to render the decoration type with
     */
    public void register(MapDecorationType type, IMapDecorationRenderer renderer) {
        IMapDecorationRenderer oldRenderer = renderers.put(type, renderer);
        if (oldRenderer != null) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    "Duplicate renderer registration for %s (old: %s, new: %s)",
                    type,
                    oldRenderer,
                    renderer));
        }
    }
}
