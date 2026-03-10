package net.neoforged.neoforge.common.extensions;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;

/**
 * Extension interface for Advancement.Builder.
 */
public interface IAdvancementBuilderExtension {

    /**
     * Builds and saves this advancement with the given consumer and id.
     */
    default AdvancementHolder save(Consumer<AdvancementHolder> saver, ResourceLocation id) {
        Advancement.Builder self = (Advancement.Builder) this;
        AdvancementHolder holder = self.build(id);
        saver.accept(holder);
        return holder;
    }
}
