package org.xiyu.reforged.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;

/**
 * NeoForge adds getModelData(BlockPos) to BlockAndTintGetter.
 * Forge keeps model data on BlockEntity (IForgeBlockEntity.getModelData()).
 * Bridge the gap by adding the method here, delegating to the block entity.
 */
@Mixin(BlockAndTintGetter.class)
public interface BlockAndTintGetterMixin extends BlockGetter {

    default ModelData getModelData(BlockPos pos) {
        BlockEntity be = getBlockEntity(pos);
        if (be != null) {
            return be.getModelData();
        }
        return ModelData.EMPTY;
    }
}
