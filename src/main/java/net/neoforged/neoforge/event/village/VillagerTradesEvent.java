package net.neoforged.neoforge.event.village;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired for each {@link VillagerProfession} to allow mods to add/remove villager trades.
 * <p>
 * The trades are organized by level (1-5), each level containing a list of {@link VillagerTrades.ItemListing}.
 */
public class VillagerTradesEvent extends Event {
    private final Int2ObjectMap<List<VillagerTrades.ItemListing>> trades;
    private final VillagerProfession type;
    private final RegistryAccess registryAccess;

    public VillagerTradesEvent(Int2ObjectMap<List<VillagerTrades.ItemListing>> trades, VillagerProfession type, RegistryAccess registryAccess) {
        this.trades = trades;
        this.type = type;
        this.registryAccess = registryAccess;
    }

    public Int2ObjectMap<List<VillagerTrades.ItemListing>> getTrades() { return trades; }
    public VillagerProfession getType() { return type; }
    public RegistryAccess getRegistryAccess() { return registryAccess; }
}
