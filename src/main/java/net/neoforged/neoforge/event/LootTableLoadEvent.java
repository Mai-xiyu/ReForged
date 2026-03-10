package net.neoforged.neoforge.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when a LootTable is loaded from JSON.
 */
public class LootTableLoadEvent extends Event {
    private final ResourceLocation name;
    private LootTable table;

    public LootTableLoadEvent(ResourceLocation name, LootTable table) {
        this.name = name;
        this.table = table;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public LootTableLoadEvent(net.minecraftforge.event.LootTableLoadEvent delegate) {
        this(delegate.getName(), delegate.getTable());
    }

    public ResourceLocation getName() {
        return name;
    }

    public LootTable getTable() {
        return table;
    }

    public void setTable(LootTable table) {
        this.table = table;
    }
}
