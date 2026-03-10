package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for DispensibleContainerItem.
 */
public interface IDispensibleContainerItemExtension {

    /**
     * Empties the container with access to the container item stack.
     */
    default boolean emptyContents(@Nullable Player player, Level level, BlockPos pos,
                                  @Nullable BlockHitResult hitResult, @Nullable ItemStack container) {
        return ((DispensibleContainerItem) this).emptyContents(player, level, pos, hitResult);
    }
}
