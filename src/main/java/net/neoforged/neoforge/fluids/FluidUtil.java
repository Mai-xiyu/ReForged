package net.neoforged.neoforge.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Utility methods for fluid interaction — filling, draining, transferring.
 */
public class FluidUtil {
    private FluidUtil() {}

    /**
     * Try to fill a container item stack from a fluid handler.
     */
    public static FluidActionResult tryFillContainer(ItemStack container, IFluidHandler fluidSource, int maxAmount, @Nullable Player player, boolean doFill) {
        // Delegates to Forge's FluidUtil, wrapping the result
        net.minecraftforge.fluids.FluidActionResult forgeResult = net.minecraftforge.fluids.FluidUtil.tryFillContainer(container, wrapToForge(fluidSource), maxAmount, player, doFill);
        return forgeResult.isSuccess() ? new FluidActionResult(forgeResult.getResult()) : FluidActionResult.FAILURE;
    }

    /**
     * Try to empty a container item stack into a fluid handler.
     */
    public static FluidActionResult tryEmptyContainer(ItemStack container, IFluidHandler fluidDestination, int maxAmount, @Nullable Player player, boolean doDrain) {
        net.minecraftforge.fluids.FluidActionResult forgeResult = net.minecraftforge.fluids.FluidUtil.tryEmptyContainer(container, wrapToForge(fluidDestination), maxAmount, player, doDrain);
        return forgeResult.isSuccess() ? new FluidActionResult(forgeResult.getResult()) : FluidActionResult.FAILURE;
    }

    /**
     * Attempt to interact with a fluid handler from a player's held item.
     */
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, IFluidHandler handler) {
        return net.minecraftforge.fluids.FluidUtil.interactWithFluidHandler(player, hand, wrapToForge(handler));
    }

    /**
     * Transfer fluid between two handlers.
     */
    public static int tryTransfer(IFluidHandler source, IFluidHandler dest, int maxAmount) {
        FluidStack drained = source.drain(maxAmount, IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty()) return 0;
        int filled = dest.fill(drained, IFluidHandler.FluidAction.SIMULATE);
        if (filled <= 0) return 0;
        drained = source.drain(filled, IFluidHandler.FluidAction.EXECUTE);
        return dest.fill(drained, IFluidHandler.FluidAction.EXECUTE);
    }

    /**
     * Wrap a NeoForge IFluidHandler to a Forge IFluidHandler for delegation.
     */
    private static net.minecraftforge.fluids.capability.IFluidHandler wrapToForge(IFluidHandler neoHandler) {
        return new net.minecraftforge.fluids.capability.IFluidHandler() {
            @Override public int getTanks() { return neoHandler.getTanks(); }
            @Override public net.minecraftforge.fluids.FluidStack getFluidInTank(int tank) {
                FluidStack neo = neoHandler.getFluidInTank(tank);
                return neo.isEmpty() ? net.minecraftforge.fluids.FluidStack.EMPTY : new net.minecraftforge.fluids.FluidStack(neo.getFluid(), neo.getAmount());
            }
            @Override public int getTankCapacity(int tank) { return neoHandler.getTankCapacity(tank); }
            @Override public boolean isFluidValid(int tank, net.minecraftforge.fluids.FluidStack stack) { return true; }
            @Override public int fill(net.minecraftforge.fluids.FluidStack resource, FluidAction action) {
                return neoHandler.fill(new FluidStack(resource.getFluid(), resource.getAmount()), action == FluidAction.EXECUTE ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
            }
            @Override public net.minecraftforge.fluids.FluidStack drain(net.minecraftforge.fluids.FluidStack resource, FluidAction action) {
                FluidStack result = neoHandler.drain(new FluidStack(resource.getFluid(), resource.getAmount()), action == FluidAction.EXECUTE ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
                return result.isEmpty() ? net.minecraftforge.fluids.FluidStack.EMPTY : new net.minecraftforge.fluids.FluidStack(result.getFluid(), result.getAmount());
            }
            @Override public net.minecraftforge.fluids.FluidStack drain(int maxDrain, FluidAction action) {
                FluidStack result = neoHandler.drain(maxDrain, action == FluidAction.EXECUTE ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
                return result.isEmpty() ? net.minecraftforge.fluids.FluidStack.EMPTY : new net.minecraftforge.fluids.FluidStack(result.getFluid(), result.getAmount());
            }
        };
    }
}
