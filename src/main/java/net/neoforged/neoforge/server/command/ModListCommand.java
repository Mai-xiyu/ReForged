package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

/**
 * /neoforge modlist — lists all loaded mods.
 */
public class ModListCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("modlist")
            .requires(cs -> cs.hasPermission(0))
            .executes(ctx -> {
                var mods = ModList.get().getMods();
                ctx.getSource().sendSuccess(() ->
                    Component.literal("Loaded mods (" + mods.size() + "):"), false);
                for (var info : mods) {
                    ctx.getSource().sendSuccess(() ->
                        Component.literal(String.format("  %s (%s) - %s",
                            info.getModId(), info.getVersion(), info.getDisplayName())),
                        false);
                }
                return mods.size();
            });
    }
}
