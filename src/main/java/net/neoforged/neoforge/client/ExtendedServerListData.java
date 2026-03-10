package net.neoforged.neoforge.client;

/**
 * Extended server list data for displaying mod info in the server list.
 */
public record ExtendedServerListData(
    String type,
    boolean isCompatible,
    int numberOfMods,
    String extraReason,
    boolean truncated
) {
    public ExtendedServerListData(String type, boolean isCompatible, int numberOfMods, String extraReason) {
        this(type, isCompatible, numberOfMods, extraReason, false);
    }
}
