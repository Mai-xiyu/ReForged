package net.neoforged.neoforge.client.event;

import java.util.List;
import java.util.ArrayList;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;

/**
 * Fired to allow mods to add custom geometry to chunk sections.
 */
public class AddSectionGeometryEvent extends net.neoforged.bus.api.Event {
    private final SectionPos sectionPos;
    private final BlockAndTintGetter level;
    private final List<AdditionalSectionRenderer> additionalRenderers = new ArrayList<>();

    public AddSectionGeometryEvent(SectionPos sectionPos, BlockAndTintGetter level) {
        this.sectionPos = sectionPos;
        this.level = level;
    }

    public SectionPos getSectionPos() { return sectionPos; }
    public BlockAndTintGetter getLevel() { return level; }
    public List<AdditionalSectionRenderer> getAdditionalRenderers() { return additionalRenderers; }

    public void addRenderer(AdditionalSectionRenderer renderer) {
        additionalRenderers.add(renderer);
    }

    @FunctionalInterface
    public interface AdditionalSectionRenderer {
        void render(SectionRenderingContext context);
    }

    /**
     * Context for rendering additional section geometry.
     */
    public record SectionRenderingContext(
            BlockAndTintGetter region,
            SectionPos sectionPos
    ) {}
}
