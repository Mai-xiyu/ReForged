package net.neoforged.neoforge.client.extensions.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side rendering extensions for {@link FluidType}.
 * Extends Forge's interface so that NeoForge mod implementations are compatible with Forge's rendering pipeline.
 * The {@link #of} methods query the Forge FluidType's registered render properties.
 */
public interface IClientFluidTypeExtensions extends net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions {

    IClientFluidTypeExtensions DEFAULT = new IClientFluidTypeExtensions() {};

    static IClientFluidTypeExtensions of(FluidState state) {
        return of((net.minecraftforge.fluids.FluidType) state.getFluidType());
    }

    static IClientFluidTypeExtensions of(Fluid fluid) {
        return of((net.minecraftforge.fluids.FluidType) fluid.getFluidType());
    }

    static IClientFluidTypeExtensions of(net.minecraftforge.fluids.FluidType type) {
        // Query Forge's registered render properties via getRenderPropertiesInternal()
        Object props = type.getRenderPropertiesInternal();
        if (props instanceof IClientFluidTypeExtensions ext) {
            return ext;
        }
        if (props instanceof net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions forgeExt) {
            // Wrap Forge's instance in our interface
            return new IClientFluidTypeExtensions() {
                @Override
                public int getTintColor() {
                    return forgeExt.getTintColor();
                }

                @Nullable
                @Override
                public ResourceLocation getStillTexture() {
                    return forgeExt.getStillTexture();
                }

                @Nullable
                @Override
                public ResourceLocation getFlowingTexture() {
                    return forgeExt.getFlowingTexture();
                }

                @Nullable
                @Override
                public ResourceLocation getOverlayTexture() {
                    return forgeExt.getOverlayTexture();
                }
            };
        }
        return DEFAULT;
    }
}
