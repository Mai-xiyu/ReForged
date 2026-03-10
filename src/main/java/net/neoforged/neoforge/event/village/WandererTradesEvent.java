package net.neoforged.neoforge.event.village;

import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired to allow mods to add trades to the wandering trader.
 */
public class WandererTradesEvent extends Event {
    private final List<VillagerTrades.ItemListing> genericTrades;
    private final List<VillagerTrades.ItemListing> rareTrades;
    private final RegistryAccess registryAccess;

    public WandererTradesEvent(List<VillagerTrades.ItemListing> genericTrades, List<VillagerTrades.ItemListing> rareTrades, RegistryAccess registryAccess) {
        this.genericTrades = genericTrades;
        this.rareTrades = rareTrades;
        this.registryAccess = registryAccess;
    }

    public List<VillagerTrades.ItemListing> getGenericTrades() { return genericTrades; }
    public List<VillagerTrades.ItemListing> getRareTrades() { return rareTrades; }
    public RegistryAccess getRegistryAccess() { return registryAccess; }
}
