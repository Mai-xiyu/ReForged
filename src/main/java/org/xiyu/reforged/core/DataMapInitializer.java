package org.xiyu.reforged.core;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.neoforged.neoforge.registries.datamaps.DataMapStorage;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.slf4j.Logger;

/**
 * Populates NeoForge built-in DataMaps from vanilla / Forge data sources
 * so NeoForge mods can query them via {@code registry.getData(type, key)}.
 */
public final class DataMapInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private DataMapInitializer() {}

    public static void populateBuiltinDataMaps() {
        populateCompostables();
        populateFurnaceFuels();
        LOGGER.info("[ReForged] Built-in DataMaps populated");
    }

    /**
     * Populate COMPOSTABLES DataMap from vanilla's {@link ComposterBlock#COMPOSTABLES}.
     */
    private static void populateCompostables() {
        int count = 0;
        for (var entry : ComposterBlock.COMPOSTABLES.object2FloatEntrySet()) {
            var itemLike = entry.getKey();
            float chance = entry.getFloatValue();
            var item = itemLike.asItem();
            var key = BuiltInRegistries.ITEM.getResourceKey(item);
            if (key.isPresent()) {
                DataMapStorage.put(Registries.ITEM, NeoForgeDataMaps.COMPOSTABLES, key.get(), new Compostable(chance));
                count++;
            }
        }
        LOGGER.debug("[ReForged] Populated {} compostable entries", count);
    }

    /**
     * Populate FURNACE_FUELS DataMap from Forge's burn time system.
     */
    @SuppressWarnings("deprecation")
    private static void populateFurnaceFuels() {
        int count = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            try {
                int burnTime = net.minecraftforge.common.ForgeHooks.getBurnTime(
                        new net.minecraft.world.item.ItemStack(item), null);
                if (burnTime > 0) {
                    var key = BuiltInRegistries.ITEM.getResourceKey(item);
                    if (key.isPresent()) {
                        DataMapStorage.put(Registries.ITEM, NeoForgeDataMaps.FURNACE_FUELS, key.get(), new FurnaceFuel(burnTime));
                        count++;
                    }
                }
            } catch (Throwable ignored) {
                // Some items may throw when creating stacks during early init
            }
        }
        LOGGER.debug("[ReForged] Populated {} furnace fuel entries", count);
    }
}
