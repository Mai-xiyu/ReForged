package org.xiyu.reforged.mixin;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Makes creative mode tab building error-tolerant.
 *
 * <p>NeoForge mods (e.g. Create) may register items whose classes fail to cast
 * correctly at runtime due to class loader differences. This wraps
 * {@code buildContents()} per-tab in try-catch so one failing tab doesn't
 * crash the entire inventory screen.</p>
 */
@Mixin(CreativeModeTabs.class)
public class CreativeModeTabsMixin {

    private static final Logger REFORGED_LOGGER = LogManager.getLogger("ReForged");

    /**
     * @reason Wrap each tab's buildContents in try-catch to prevent one mod's failure from crashing the game
     * @author ReForged
     */
    @Overwrite(remap = false)
    private static void buildAllTabContents(CreativeModeTab.ItemDisplayParameters params) {
		CreativeModeTabs.allTabs().stream()
            .filter(tab -> tab.getType() == CreativeModeTab.Type.CATEGORY)
            .forEach(tab -> {
                try {
                    tab.buildContents(params);
                } catch (Exception e) {
                    REFORGED_LOGGER.error("[ReForged] Failed to build creative tab contents: {}", e.getMessage());
                }
            });
		CreativeModeTabs.allTabs().stream()
            .filter(tab -> tab.getType() == CreativeModeTab.Type.SEARCH)
            .forEach(tab -> {
                try {
                    tab.buildContents(params);
                } catch (Exception e) {
                    REFORGED_LOGGER.error("[ReForged] Failed to build search tab contents: {}", e.getMessage());
                }
            });
    }
}
