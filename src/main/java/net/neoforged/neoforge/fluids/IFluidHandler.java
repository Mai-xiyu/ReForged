package net.neoforged.neoforge.fluids;

/**
 * Capability interface for handling fluids.
 * Provides methods for querying tanks, filling, and draining fluids.
 */
public interface IFluidHandler {

    /**
     * @return the number of internal tanks
     */
    int getTanks();

    /**
     * @return the FluidStack in the given tank. EMPTY if the tank is empty.
     */
    FluidStack getFluidInTank(int tank);

    /**
     * @return the max capacity of the given tank
     */
    int getTankCapacity(int tank);

    /**
     * @return true if the given FluidStack is valid for the given tank
     */
    boolean isFluidValid(int tank, FluidStack stack);

    /**
     * Fills the handler with the given FluidStack.
     * @return the amount of fluid that was actually filled
     */
    int fill(FluidStack resource, FluidAction action);

    /**
     * Drains a specific fluid from the handler.
     * @return the FluidStack that was drained
     */
    FluidStack drain(FluidStack resource, FluidAction action);

    /**
     * Drains up to maxDrain fluid from the handler.
     * @return the FluidStack that was drained
     */
    FluidStack drain(int maxDrain, FluidAction action);

    /**
     * Represents the two fluid action types: execute (do it) or simulate (test).
     */
    enum FluidAction {
        EXECUTE,
        SIMULATE;

        public boolean execute() { return this == EXECUTE; }
        public boolean simulate() { return this == SIMULATE; }
    }
}
