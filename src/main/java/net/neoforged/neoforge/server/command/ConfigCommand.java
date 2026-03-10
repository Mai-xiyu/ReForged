package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * /neoforge config — stub config command.
 */
public class ConfigCommand {
    private ConfigCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("config")
            .requires(cs -> cs.hasPermission(0))
            .executes(ctx -> {
                ctx.getSource().sendSuccess(() -> Component.literal("NeoForge config system is bridged to Forge."), false);
                return 1;
            });
    }
}
