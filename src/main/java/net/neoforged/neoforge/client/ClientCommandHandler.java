package net.neoforged.neoforge.client;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

/**
 * Stub: Handler for client-side commands.
 */
public class ClientCommandHandler {
    private ClientCommandHandler() {}

    private static CommandDispatcher<CommandSourceStack> dispatcher;

    static void init() {
        dispatcher = new CommandDispatcher<>();
    }

    public static CommandDispatcher<CommandSourceStack> getDispatcher() {
        if (dispatcher == null) {
            dispatcher = new CommandDispatcher<>();
        }
        return dispatcher;
    }
}
