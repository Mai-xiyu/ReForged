package net.neoforged.neoforge.event;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: Fired after a command is parsed but before execution.
 */
public class CommandEvent extends Event {
    private ParseResults<CommandSourceStack> parse;
    @Nullable
    private Throwable exception;

    public CommandEvent(ParseResults<CommandSourceStack> parse) {
        this.parse = parse;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public CommandEvent(net.minecraftforge.event.CommandEvent delegate) {
        this(delegate.getParseResults());
    }

    public ParseResults<CommandSourceStack> getParseResults() {
        return parse;
    }

    public void setParseResults(ParseResults<CommandSourceStack> parse) {
        this.parse = parse;
    }

    @Nullable
    public Throwable getException() {
        return exception;
    }

    public void setException(@Nullable Throwable exception) {
        this.exception = exception;
    }
}
