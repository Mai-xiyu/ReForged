package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Fired after {@link Mob#finalizeSpawn} to allow modification of spawn data.
 */
public class FinalizeSpawnEvent extends Event implements ICancellableEvent {
    private final Mob entity;
    private final ServerLevelAccessor level;
    private final double x, y, z;
    private DifficultyInstance difficulty;
    private final MobSpawnType spawnType;
    @Nullable private SpawnGroupData spawnData;
    private boolean spawnCancelled;

    public FinalizeSpawnEvent(Mob entity, ServerLevelAccessor level, double x, double y, double z,
            DifficultyInstance difficulty, MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnData) {
        this.entity = entity;
        this.level = level;
        this.x = x;
        this.y = y;
        this.z = z;
        this.difficulty = difficulty;
        this.spawnType = spawnType;
        this.spawnData = spawnData;
    }

    public Mob getEntity() { return entity; }
    public ServerLevelAccessor getLevel() { return level; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public DifficultyInstance getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyInstance inst) { this.difficulty = inst; }
    public MobSpawnType getSpawnType() { return spawnType; }
    @Nullable public SpawnGroupData getSpawnData() { return spawnData; }
    public void setSpawnData(@Nullable SpawnGroupData data) { this.spawnData = data; }
    public void setSpawnCancelled(boolean cancel) { this.spawnCancelled = cancel; }
    public boolean isSpawnCancelled() { return spawnCancelled; }
}
