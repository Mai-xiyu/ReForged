package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Extension interface for PlayerList.
 */
public interface IPlayerListExtension {

    default PlayerList self() {
        return (PlayerList) this;
    }
}
