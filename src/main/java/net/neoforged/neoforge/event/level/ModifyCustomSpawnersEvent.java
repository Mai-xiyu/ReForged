package net.neoforged.neoforge.event.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Fired to allow mods to add custom mob spawners to a {@link ServerLevel}.
 */
public class ModifyCustomSpawnersEvent extends Event {
    private final ServerLevel level;
    private final List<CustomSpawner> customSpawners;

    public ModifyCustomSpawnersEvent(ServerLevel level, List<CustomSpawner> customSpawners) {
        this.level = level;
        this.customSpawners = new ArrayList<>(customSpawners);
    }

    public ServerLevel getLevel() { return level; }
    public List<CustomSpawner> getCustomSpawners() { return customSpawners; }
}
