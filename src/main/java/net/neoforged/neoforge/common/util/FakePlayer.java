package net.neoforged.neoforge.common.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

/**
 * A server-side fake player used for automated actions (block breaking, item use, etc.).
 */
public class FakePlayer extends ServerPlayer {
    public FakePlayer(ServerLevel level, GameProfile profile) {
        super(level.getServer(), level, profile, ClientInformation.createDefault());
    }

    @Override public void displayClientMessage(Component message, boolean overlay) {}
    @Override public void awardStat(Stat<?> stat, int amount) {}
    @Override public boolean isInvulnerableTo(DamageSource source) { return true; }
    @Override public boolean canHarmPlayer(net.minecraft.world.entity.player.Player other) { return false; }
    @Override public void die(DamageSource source) {}
    @Override public void tick() {}
    @Override @Nullable public net.minecraft.server.MinecraftServer getServer() { return this.server; }
}
