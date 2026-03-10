package net.neoforged.neoforge.client.extensions.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side rendering extensions for {@link FluidType}.
 * Provides texture locations and tint colors for fluid rendering.
 */
public interface IClientFluidTypeExtensions {

    IClientFluidTypeExtensions DEFAULT = new IClientFluidTypeExtensions() {};

    static IClientFluidTypeExtensions of(FluidState state) {
        return DEFAULT;
    }

    static IClientFluidTypeExtensions of(Fluid fluid) {
        return DEFAULT;
    }

    static IClientFluidTypeExtensions of(FluidType type) {
        // In a full implementation, this would look up the registered extensions.
        // For the shim, return the default.
        return DEFAULT;
    }

    /**
     * Returns the tint color applied to the fluid texture (ARGB).
     */
    default int getTintColor() {
        return 0xFFFFFFFF;
    }

    /**
     * Returns the still texture location for this fluid.
     */
    @Nullable
    default ResourceLocation getStillTexture() {
        return null;
    }

    /**
     * Returns the flowing texture location for this fluid.
     */
    @Nullable
    default ResourceLocation getFlowingTexture() {
        return null;
    }

    /**
     * Returns the overlay texture for when the fluid is adjacent to non-opaque blocks.
     */
    @Nullable
    default ResourceLocation getOverlayTexture() {
        return null;
    }

    @Nullable
    default ResourceLocation getStillTexture(FluidStack stack) {
        return getStillTexture();
    }

    @Nullable
    default ResourceLocation getFlowingTexture(FluidStack stack) {
        return getFlowingTexture();
    }

    @Nullable
    default ResourceLocation getOverlayTexture(FluidStack stack) {
        return getOverlayTexture();
    }

    default int getTintColor(FluidStack stack) {
        return getTintColor();
    }
}
