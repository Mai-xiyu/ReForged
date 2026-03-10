package net.neoforged.neoforge.client.extensions;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/**
 * Extension interface for {@link net.minecraft.client.renderer.blockentity.BlockEntityRenderer}.
 */
public interface IBlockEntityRendererExtension<T extends BlockEntity> {

    /**
     * Returns the render bounding box for the given block entity.
     * Used for frustum culling. Defaults to a unit cube at the block position.
     */
    default AABB getRenderBoundingBox(T blockEntity) {
        return new AABB(
                blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ(),
                blockEntity.getBlockPos().getX() + 1, blockEntity.getBlockPos().getY() + 1, blockEntity.getBlockPos().getZ() + 1
        );
    }
}
