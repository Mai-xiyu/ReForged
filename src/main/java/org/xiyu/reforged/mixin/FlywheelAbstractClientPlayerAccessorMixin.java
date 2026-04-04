package org.xiyu.reforged.mixin;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Injects Flywheel's AbstractClientPlayerAccessor.flywheel$getPlayerInfo()
 * so Flywheel can access the player's skin/cape data for rendering.
 */
@Mixin(value = AbstractClientPlayer.class, remap = false)
public abstract class FlywheelAbstractClientPlayerAccessorMixin {

    @Shadow(remap = false)
    protected abstract PlayerInfo getPlayerInfo();

    public PlayerInfo flywheel$getPlayerInfo() {
        return this.getPlayerInfo();
    }
}
