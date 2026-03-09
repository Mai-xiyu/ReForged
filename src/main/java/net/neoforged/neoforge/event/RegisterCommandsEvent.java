package net.neoforged.neoforge.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.Event;

/** Proxy: NeoForge RegisterCommandsEvent */
public class RegisterCommandsEvent extends Event {
	private final CommandDispatcher<CommandSourceStack> dispatcher;
	private final Commands.CommandSelection environment;
	private final CommandBuildContext context;

	public RegisterCommandsEvent() {
		this.dispatcher = null;
		this.environment = null;
		this.context = null;
	}

	public RegisterCommandsEvent(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandBuildContext context) {
		this.dispatcher = dispatcher;
		this.environment = environment;
		this.context = context;
	}

	public RegisterCommandsEvent(net.minecraftforge.event.RegisterCommandsEvent delegate) {
		this(delegate.getDispatcher(), delegate.getCommandSelection(), delegate.getBuildContext());
	}

	public CommandDispatcher<CommandSourceStack> getDispatcher() {
		return dispatcher;
	}

	public Commands.CommandSelection getCommandSelection() {
		return environment;
	}

	public CommandBuildContext getBuildContext() {
		return context;
	}
}
