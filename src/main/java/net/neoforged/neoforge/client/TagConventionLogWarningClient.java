package net.neoforged.neoforge.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeConfig;
import net.neoforged.neoforge.common.TagConventionLogWarning;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Client-side tag convention log warning system.
 * Warns about untranslated item tags when in development mode.
 */
public final class TagConventionLogWarningClient {
    private TagConventionLogWarningClient() {}

    private static final Logger LOGGER = LogManager.getLogger();

    static void init() {
        IEventBus forgeBus = NeoForge.EVENT_BUS;
        setupUntranslatedItemTagWarning(forgeBus);
    }

    static void setupUntranslatedItemTagWarning(IEventBus forgeBus) {
        forgeBus.addListener((ServerStartingEvent event) -> {
            TagConventionLogWarning.LogWarningMode mode = NeoForgeConfig.COMMON.logUntranslatedItemTagWarnings.get();
            if (FMLEnvironment.dist == Dist.CLIENT && mode != TagConventionLogWarning.LogWarningMode.SILENCED) {
                boolean isConfigSetToDev = mode == TagConventionLogWarning.LogWarningMode.DEV_SHORT ||
                        mode == TagConventionLogWarning.LogWarningMode.DEV_VERBOSE;

                if (!FMLLoader.isProduction() == isConfigSetToDev) {
                    List<TagKey<?>> untranslatedTags = new ArrayList<>();
                    RegistryAccess.Frozen registryAccess = event.getServer().registryAccess();
                    extractUnregisteredModdedTags(registryAccess.registryOrThrow(Registries.ITEM), untranslatedTags);

                    if (!untranslatedTags.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("\n\tDev warning - Untranslated Item Tags detected. Please translate your item tags.\n");
                        sb.append("\tFormat: tag.item.<namespace>.<path> (slashes in path become periods).\n");

                        boolean isVerbose = mode == TagConventionLogWarning.LogWarningMode.DEV_VERBOSE ||
                                mode == TagConventionLogWarning.LogWarningMode.PROD_VERBOSE;

                        if (isVerbose) {
                            sb.append("\nUntranslated item tags:");
                            for (TagKey<?> tagKey : untranslatedTags) {
                                sb.append("\n     ").append(tagKey.location());
                            }
                        }

                        LOGGER.warn(sb.toString());
                    }
                }
            }
        });
    }

    private static void extractUnregisteredModdedTags(Registry<?> registry, List<TagKey<?>> untranslatedTags) {
        registry.getTagNames().forEach(tagKey -> {
            if (tagKey.location().getNamespace().equals("minecraft")) {
                return;
            }
            String translationKey = Tags.getTagTranslationKey(tagKey);
            if (!I18n.exists(translationKey)) {
                untranslatedTags.add(tagKey);
            }
        });
    }
}
