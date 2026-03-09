package net.neoforged.neoforge.capabilities;

@FunctionalInterface
public interface ICapabilityInvalidationListener {
    boolean onInvalidate();
}