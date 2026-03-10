package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Fired to control phantom spawning for a player.
 */
public class PlayerSpawnPhantomsEvent extends PlayerEvent {
    private int phantomsToSpawn;
    private Result result = Result.DEFAULT;

    public PlayerSpawnPhantomsEvent(Player player, int phantomsToSpawn) {
        super(player);
        this.phantomsToSpawn = phantomsToSpawn;
    }

    public int getPhantomsToSpawn() { return phantomsToSpawn; }
    public void setPhantomsToSpawn(int count) { this.phantomsToSpawn = count; }
    public void setSpawnResult(Result result) { this.result = result; }
    public Result getSpawnResult() { return result; }

    public boolean shouldSpawnPhantoms(ServerLevel level, BlockPos pos) {
        if (result == Result.ALLOW) return true;
        return result == Result.DEFAULT && (!level.dimensionType().hasSkyLight() || pos.getY() >= level.getSeaLevel() && level.canSeeSky(pos));
    }

    public enum Result { ALLOW, DEFAULT, DENY }
}
