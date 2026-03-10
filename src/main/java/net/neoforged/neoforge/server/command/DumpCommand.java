package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * /neoforge dump — dumps contents of a built-in registry to chat.
 */
public class DumpCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("dump")
            .requires(cs -> cs.hasPermission(2))
            .then(Commands.literal("registry")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("Available registries:"), false);
                    int count = 0;
                    for (var entry : BuiltInRegistries.REGISTRY.entrySet()) {
                        ResourceLocation key = entry.getKey().location();
                        Registry<?> reg = entry.getValue();
                        int size = reg.size();
                        ctx.getSource().sendSuccess(() ->
                            Component.literal(String.format("  %s (%d entries)", key, size)), false);
                        count++;
                    }
                    final int total = count;
                    ctx.getSource().sendSuccess(() ->
                        Component.literal("Total: " + total + " registries"), false);
                    return count;
                }));
    }
}
