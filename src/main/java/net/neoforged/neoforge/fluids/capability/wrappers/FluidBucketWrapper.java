package net.neoforged.neoforge.fluids.capability.wrappers;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * FluidHandler wrapper for vanilla and modded bucket items.
 */
public class FluidBucketWrapper implements IFluidHandlerItem {
    protected ItemStack container;

    public FluidBucketWrapper(ItemStack container) {
        this.container = container;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    public Fluid getFluid() {
        if (container.getItem() instanceof BucketItem bucket) {
            return bucket.getFluid();
        }
        return Fluids.EMPTY;
    }

    @Override public int getTanks() { return 1; }

    @Override
    public FluidStack getFluidInTank(int tank) {
        Fluid fluid = getFluid();
        return fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, 1000);
    }

    @Override public int getTankCapacity(int tank) { return 1000; }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { return true; }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < 1000 || resource.isEmpty()) return 0;
        if (getFluid() != Fluids.EMPTY) return 0; // already filled
        // Get the bucket item for this fluid
        net.minecraft.world.item.Item filledBucket = resource.getFluid().getBucket();
        if (filledBucket == Items.AIR) return 0; // fluid has no bucket form
        if (action.execute()) {
            container = new ItemStack(filledBucket);
        }
        return 1000;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        Fluid fluid = getFluid();
        if (fluid == Fluids.EMPTY || !resource.getFluid().isSame(fluid) || resource.getAmount() < 1000) return FluidStack.EMPTY;
        FluidStack drained = new FluidStack(fluid, 1000);
        if (action.execute()) container = new ItemStack(Items.BUCKET);
        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        Fluid fluid = getFluid();
        if (fluid == Fluids.EMPTY || maxDrain < 1000) return FluidStack.EMPTY;
        FluidStack drained = new FluidStack(fluid, 1000);
        if (action.execute()) container = new ItemStack(Items.BUCKET);
        return drained;
    }
}
