package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Root /neoforge command, dispatching to subcommands.
 */
public class NeoForgeCommand {
    public NeoForgeCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            LiteralArgumentBuilder.<CommandSourceStack>literal("neoforge")
                .then(TPSCommand.register())
                .then(EntityCommand.register())
                .then(DimensionsCommand.register())
                .then(DumpCommand.register())
                .then(ModListCommand.register())
                .then(TagsCommand.register())
                .then(ConfigCommand.register())
        );
    }
}
