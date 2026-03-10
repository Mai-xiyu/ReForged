package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a player's respawn position is determined.
 */
public class PlayerRespawnPositionEvent extends PlayerEvent {
    private ResourceKey<Level> dimension;
    @Nullable private BlockPos respawnPos;
    private float respawnAngle;
    private final boolean isEndConquered;

    public PlayerRespawnPositionEvent(ServerPlayer player, ResourceKey<Level> dimension,
            @Nullable BlockPos respawnPos, float respawnAngle, boolean isEndConquered) {
        super(player);
        this.dimension = dimension;
        this.respawnPos = respawnPos;
        this.respawnAngle = respawnAngle;
        this.isEndConquered = isEndConquered;
    }

    public ResourceKey<Level> getDimension() { return dimension; }
    public void setDimension(ResourceKey<Level> dimension) { this.dimension = dimension; }
    @Nullable public BlockPos getRespawnPos() { return respawnPos; }
    public void setRespawnPos(@Nullable BlockPos pos) { this.respawnPos = pos; }
    public float getRespawnAngle() { return respawnAngle; }
    public void setRespawnAngle(float angle) { this.respawnAngle = angle; }
    public boolean isEndConquered() { return isEndConquered; }
}
