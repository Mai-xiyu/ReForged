package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * /neoforge dimensions — lists all loaded dimensions.
 */
public class DimensionsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("dimensions")
            .requires(cs -> cs.hasPermission(0))
            .executes(ctx -> {
                ctx.getSource().sendSuccess(() -> Component.literal("Loaded dimensions:"), false);
                for (ServerLevel level : ctx.getSource().getServer().getAllLevels()) {
                    ctx.getSource().sendSuccess(() ->
                        Component.literal("  " + level.dimension().location()), false);
                }
                return 1;
            });
    }
}
