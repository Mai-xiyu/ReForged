package org.xiyu.reforged.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntity.class)
public abstract class BlockEntityCapabilityMixin {

    public void invalidateCapabilities() {
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        Level level = blockEntity.getLevel();
        if (level != null) {
            ((ILevelExtension) level).invalidateCapabilities(blockEntity.getBlockPos());
        }
    }
}