package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

/**
 * Extension interface for {@link AbstractMinecart}.
 */
public interface IAbstractMinecartExtension {

    float DEFAULT_MAX_SPEED_AIR_LATERAL = 0.4f;
    float DEFAULT_MAX_SPEED_AIR_VERTICAL = -1.0f;
    double DEFAULT_AIR_DRAG = 0.95;

    /**
     * Returns the current rail position for this minecart.
     */
    default BlockPos getCurrentRailPosition() {
        AbstractMinecart self = (AbstractMinecart) this;
        int x = (int) Math.floor(self.getX());
        int y = (int) Math.floor(self.getY());
        int z = (int) Math.floor(self.getZ());
        return new BlockPos(x, y, z);
    }

    double getMaxSpeedWithRail();

    void moveMinecartOnRail(BlockPos pos);

    boolean canUseRail();

    void setCanUseRail(boolean use);

    default boolean shouldDoRailFunctions() {
        return true;
    }

    default boolean isPoweredCart() {
        return ((AbstractMinecart) this).getMinecartType() == AbstractMinecart.Type.FURNACE;
    }

    default boolean canBeRidden() {
        return ((AbstractMinecart) this).getMinecartType() == AbstractMinecart.Type.RIDEABLE;
    }

    default float getMaxCartSpeedOnRail() {
        return 1.2f;
    }

    float getCurrentCartSpeedCapOnRail();

    void setCurrentCartSpeedCapOnRail(float value);

    float getMaxSpeedAirLateral();

    void setMaxSpeedAirLateral(float value);

    float getMaxSpeedAirVertical();

    void setMaxSpeedAirVertical(float value);

    double getDragAir();

    void setDragAir(double value);

    default double getSlopeAdjustment() {
        return 0.0078125D;
    }

    default int getComparatorLevel() {
        return -1;
    }
}
