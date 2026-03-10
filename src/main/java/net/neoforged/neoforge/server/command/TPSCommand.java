package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/**
 * /neoforge tps — shows ticks-per-second and mean tick time per dimension.
 */
public class TPSCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("tps")
            .requires(cs -> cs.hasPermission(0))
            .executes(ctx -> {
                MinecraftServer server = ctx.getSource().getServer();
                double meanTickTime = mean(server.getTickTimesNanos()) * 1.0E-6D;
                double tps = Math.min(20.0, 1000.0 / Math.max(meanTickTime, 50.0));

                ctx.getSource().sendSuccess(() ->
                    Component.literal(String.format("Overall: %.1f TPS (%.2f ms/tick)", tps, meanTickTime)),
                    false);

                for (ServerLevel level : server.getAllLevels()) {
                    long[] times = server.getTickTime(level.dimension());
                    if (times == null) continue;
                    double levelMean = mean(times) * 1.0E-6D;
                    double levelTps = Math.min(20.0, 1000.0 / Math.max(levelMean, 50.0));
                    ctx.getSource().sendSuccess(() ->
                        Component.literal(String.format("  %s: %.1f TPS (%.2f ms/tick)",
                            level.dimension().location(), levelTps, levelMean)),
                        false);
                }
                return 1;
            });
    }

    private static double mean(long[] values) {
        if (values == null || values.length == 0) return 0;
        long sum = 0;
        for (long v : values) sum += v;
        return (double) sum / values.length;
    }
}
