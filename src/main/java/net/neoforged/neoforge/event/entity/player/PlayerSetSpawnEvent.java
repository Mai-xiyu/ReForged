package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.Nullable;

@Cancelable
public class PlayerSetSpawnEvent extends PlayerEvent implements ICancellableEvent {
    @Nullable
    private final net.minecraftforge.event.entity.player.PlayerSetSpawnEvent forgeDelegate;
    private final ResourceKey<Level> spawnLevel;
    private final boolean forced;
    @Nullable
    private final BlockPos newSpawn;

    public PlayerSetSpawnEvent() {
        super();
        this.forgeDelegate = null;
        this.spawnLevel = null;
        this.forced = false;
        this.newSpawn = null;
    }

    public PlayerSetSpawnEvent(Player player, ResourceKey<Level> spawnLevel, @Nullable BlockPos newSpawn, boolean forced) {
        super(player);
        this.forgeDelegate = null;
        this.spawnLevel = spawnLevel;
        this.newSpawn = newSpawn;
        this.forced = forced;
    }

    public PlayerSetSpawnEvent(net.minecraftforge.event.entity.player.PlayerSetSpawnEvent forge) {
        super(forge);
        this.forgeDelegate = forge;
        this.spawnLevel = forge.getSpawnLevel();
        this.newSpawn = forge.getNewSpawn();
        this.forced = forge.isForced();
    }

    public boolean isForced() {
        return forced;
    }

    @Nullable
    public BlockPos getNewSpawn() {
        return newSpawn;
    }

    public ResourceKey<Level> getSpawnLevel() {
        return spawnLevel;
    }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        if (forgeDelegate != null) {
            forgeDelegate.setCanceled(canceled);
        }
    }
}
