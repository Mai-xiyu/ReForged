package net.neoforged.neoforge.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Proxy: NeoForge's common Tags.
 * <p>
 * NeoForge uses "c" (convention) namespace tags. These correspond to the same
 * convention tags used by Forge's {@code net.minecraftforge.common.Tags}.
 * This shim creates {@link TagKey} instances that match what NeoForge mods expect.
 * </p>
 */
public final class Tags {
    private Tags() {}

    public static final class Items {
        private Items() {}

        // ── Cobblestones ──
        public static final TagKey<Item> COBBLESTONES = tag("cobblestones");

        // ── Ingots ──
        public static final TagKey<Item> INGOTS = tag("ingots");
        public static final TagKey<Item> INGOTS_COPPER = tag("ingots/copper");
        public static final TagKey<Item> INGOTS_IRON = tag("ingots/iron");
        public static final TagKey<Item> INGOTS_GOLD = tag("ingots/gold");
        public static final TagKey<Item> INGOTS_NETHERITE = tag("ingots/netherite");

        // ── Gems ──
        public static final TagKey<Item> GEMS = tag("gems");
        public static final TagKey<Item> GEMS_DIAMOND = tag("gems/diamond");
        public static final TagKey<Item> GEMS_LAPIS = tag("gems/lapis");
        public static final TagKey<Item> GEMS_EMERALD = tag("gems/emerald");
        public static final TagKey<Item> GEMS_AMETHYST = tag("gems/amethyst");
        public static final TagKey<Item> GEMS_QUARTZ = tag("gems/quartz");
        public static final TagKey<Item> GEMS_PRISMARINE = tag("gems/prismarine");

        // ── Obsidian ──
        public static final TagKey<Item> OBSIDIANS = tag("obsidians");

        // ── Dusts ──
        public static final TagKey<Item> DUSTS = tag("dusts");
        public static final TagKey<Item> DUSTS_REDSTONE = tag("dusts/redstone");
        public static final TagKey<Item> DUSTS_GLOWSTONE = tag("dusts/glowstone");

        // ── Nuggets ──
        public static final TagKey<Item> NUGGETS = tag("nuggets");
        public static final TagKey<Item> NUGGETS_IRON = tag("nuggets/iron");
        public static final TagKey<Item> NUGGETS_GOLD = tag("nuggets/gold");

        // ── Raw materials ──
        public static final TagKey<Item> RAW_MATERIALS = tag("raw_materials");
        public static final TagKey<Item> RAW_MATERIALS_IRON = tag("raw_materials/iron");
        public static final TagKey<Item> RAW_MATERIALS_GOLD = tag("raw_materials/gold");
        public static final TagKey<Item> RAW_MATERIALS_COPPER = tag("raw_materials/copper");

        // ── Ores ──
        public static final TagKey<Item> ORES = tag("ores");

        // ── Storage blocks ──
        public static final TagKey<Item> STORAGE_BLOCKS = tag("storage_blocks");

        // ── Strings / Rods / Bones / Leather / Feathers ──
        public static final TagKey<Item> STRINGS = tag("strings");
        public static final TagKey<Item> RODS = tag("rods");
        public static final TagKey<Item> RODS_WOODEN = tag("rods/wooden");
        public static final TagKey<Item> RODS_BLAZE = tag("rods/blaze");
        public static final TagKey<Item> BONES = tag("bones");
        public static final TagKey<Item> LEATHER = tag("leather");
        public static final TagKey<Item> FEATHERS = tag("feathers");

        // ── Crops ──
        public static final TagKey<Item> CROPS = tag("crops");
        public static final TagKey<Item> CROPS_WHEAT = tag("crops/wheat");

        // ── Seeds ──
        public static final TagKey<Item> SEEDS = tag("seeds");

        // ── Dyes ──
        public static final TagKey<Item> DYES = tag("dyes");

        // ── Buckets ──
        public static final TagKey<Item> BUCKETS = tag("buckets");
        public static final TagKey<Item> BUCKETS_WATER = tag("buckets/water");
        public static final TagKey<Item> BUCKETS_LAVA = tag("buckets/lava");
        public static final TagKey<Item> BUCKETS_EMPTY = tag("buckets/empty");

        // ── Misc ──
        public static final TagKey<Item> ENDER_PEARLS = tag("ender_pearls");
        public static final TagKey<Item> SLIME_BALLS = tag("slime_balls");
        public static final TagKey<Item> NETHER_STARS = tag("nether_stars");
        public static final TagKey<Item> EGGS = tag("eggs");
        public static final TagKey<Item> BRICKS = tag("bricks");

        // ── Tools & Armor ──
        public static final TagKey<Item> TOOLS = tag("tools");
        public static final TagKey<Item> TOOLS_SWORDS = tag("tools/swords");
        public static final TagKey<Item> TOOLS_AXES = tag("tools/axes");
        public static final TagKey<Item> TOOLS_PICKAXES = tag("tools/pickaxes");
        public static final TagKey<Item> TOOLS_SHOVELS = tag("tools/shovels");
        public static final TagKey<Item> TOOLS_HOES = tag("tools/hoes");
        public static final TagKey<Item> TOOLS_SHIELDS = tag("tools/shields");
        public static final TagKey<Item> TOOLS_BOWS = tag("tools/bows");
        public static final TagKey<Item> TOOLS_CROSSBOWS = tag("tools/crossbows");
        public static final TagKey<Item> TOOLS_FISHING_RODS = tag("tools/fishing_rods");
        public static final TagKey<Item> TOOLS_TRIDENTS = tag("tools/tridents");

        public static final TagKey<Item> ARMORS = tag("armors");

        // ── Foods ──
        public static final TagKey<Item> FOODS = tag("foods");

        // ── Chests / Barrels / Bookshelves ──
        public static final TagKey<Item> CHESTS = tag("chests");
        public static final TagKey<Item> CHESTS_WOODEN = tag("chests/wooden");
        public static final TagKey<Item> BARRELS = tag("barrels");
        public static final TagKey<Item> BARRELS_WOODEN = tag("barrels/wooden");
        public static final TagKey<Item> BOOKSHELVES = tag("bookshelves");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }

    public static final class Blocks {
        private Blocks() {}

        public static final TagKey<Block> COBBLESTONES = tag("cobblestones");
        public static final TagKey<Block> OBSIDIANS = tag("obsidians");
        public static final TagKey<Block> ORES = tag("ores");
        public static final TagKey<Block> STORAGE_BLOCKS = tag("storage_blocks");
        public static final TagKey<Block> CHESTS = tag("chests");
        public static final TagKey<Block> CHESTS_WOODEN = tag("chests/wooden");
        public static final TagKey<Block> BARRELS = tag("barrels");
        public static final TagKey<Block> BARRELS_WOODEN = tag("barrels/wooden");
        public static final TagKey<Block> BOOKSHELVES = tag("bookshelves");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }
}
