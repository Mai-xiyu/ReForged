package net.neoforged.neoforge.common.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

/**
 * Extended {@link Item.TooltipContext} used when generating attribute tooltips.
 */
public interface AttributeTooltipContext extends Item.TooltipContext {
    /**
     * {@return the player for whom tooltips are being generated for, if known}
     */
    @Nullable
    Player player();

    /**
     * {@return the current tooltip flag}
     */
    TooltipFlag flag();

    static AttributeTooltipContext of(@Nullable Player player, Item.TooltipContext itemCtx, TooltipFlag flag) {
        return new AttributeTooltipContext() {
            @Override
            public HolderLookup.Provider registries() {
                return itemCtx.registries();
            }

            @Override
            public float tickRate() {
                return itemCtx.tickRate();
            }

            @Override
            public MapItemSavedData mapData(MapId id) {
                return itemCtx.mapData(id);
            }

            @Nullable
            @Override
            public Player player() {
                return player;
            }

            @Override
            public TooltipFlag flag() {
                return flag;
            }
        };
    }
}
