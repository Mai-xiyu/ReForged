package net.neoforged.neoforge.energy;

/**
 * An empty {@link IEnergyStorage} that cannot store, receive, or extract energy.
 * Useful as a default/null-object.
 */
public final class EmptyEnergyStorage implements IEnergyStorage {
    public static final EmptyEnergyStorage INSTANCE = new EmptyEnergyStorage();

    private EmptyEnergyStorage() {}

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) { return 0; }

    @Override
    public int getEnergyStored() { return 0; }

    @Override
    public int getMaxEnergyStored() { return 0; }

    @Override
    public boolean canExtract() { return false; }

    @Override
    public boolean canReceive() { return false; }
}
