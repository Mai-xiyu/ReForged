package net.neoforged.neoforge.common.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.extensions.IAdvancementBuilderExtension;

/**
 * An extension of the vanilla {@code AdvancementProvider} to provide a feature-complete
 * experience to generate modded advancements.
 */
public class AdvancementProvider extends net.minecraft.data.advancements.AdvancementProvider {

    public AdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                               ExistingFileHelper existingFileHelper, List<AdvancementGenerator> subProviders) {
        super(output, registries, subProviders.stream()
                .map(generator -> generator.toSubProvider(existingFileHelper)).toList());
    }

    /**
     * An interface used to generate modded advancements. Parallel to vanilla's
     * {@link AdvancementSubProvider} with access to the {@link ExistingFileHelper}.
     */
    public interface AdvancementGenerator {
        void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver,
                      ExistingFileHelper existingFileHelper);

        default AdvancementSubProvider toSubProvider(ExistingFileHelper existingFileHelper) {
            return (registries, saver) -> this.generate(registries, saver, existingFileHelper);
        }
    }
}
