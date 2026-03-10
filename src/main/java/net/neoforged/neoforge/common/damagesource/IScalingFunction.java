package net.neoforged.neoforge.common.damagesource;

import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

/**
 * Allows finer control over damage scaling instead of hard-coded vanilla defaults.
 */
@FunctionalInterface
public interface IScalingFunction {
    @SuppressWarnings("deprecation")
    IScalingFunction DEFAULT = (source, target, amount, difficulty) -> {
        if (source.scalesWithDifficulty()) {
            return switch (target.level().getDifficulty()) {
                case PEACEFUL -> 0.0F;
                case EASY -> Math.min(amount / 2.0F + 1.0F, amount);
                case NORMAL -> amount;
                case HARD -> amount * 1.5F;
            };
        }
        return amount;
    };

    /**
     * Scales the incoming damage amount based on the current difficulty.
     *
     * @param source     The source of the incoming damage.
     * @param target     The player which is being attacked.
     * @param amount     The amount of damage being dealt.
     * @param difficulty The current game difficulty.
     * @return The scaled damage value.
     */
    float scaleDamage(DamageSource source, Player target, float amount, Difficulty difficulty);
}
