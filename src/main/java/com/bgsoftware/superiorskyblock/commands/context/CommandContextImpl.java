package com.bgsoftware.superiorskyblock.commands.context;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.commands.arguments.ArgumentsMap;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class CommandContextImpl implements CommandContext {

    private final CommandSender dispatcher;
    private final ArgumentsMap arguments;

    public CommandContextImpl(CommandSender dispatcher, ArgumentsMap arguments) {
        this.dispatcher = dispatcher;
        this.arguments = arguments;
    }

    public CommandContextImpl(CommandContextImpl other) {
        this.dispatcher = other.dispatcher;
        this.arguments = other.arguments;
    }

    @Override
    public CommandSender getDispatcher() {
        return this.dispatcher;
    }

    @Override
    public final <E> Optional<E> getOptionalArgument(String identifier, Class<E> argumentResultType) {
        return Optional.ofNullable(getArgumentInternal(identifier, argumentResultType));
    }

    @Override
    public final <E> E getRequiredArgument(String identifier, Class<E> argumentResultType) {
        E argument = getArgumentInternal(identifier, argumentResultType);
        if (argument == null)
            throw new IllegalArgumentException("Cannot find argument for " + identifier);
        return argument;
    }

    @Override
    public final String getInputArgument(String identifier) {
        String argument = this.arguments.getArgument(identifier);
        if (argument == null)
            throw new IllegalArgumentException("Cannot find argument for " + identifier);
        return argument;
    }

    @Override
    public final int getArgumentsCount() {
        return this.arguments.size();
    }

    @Nullable
    private <E> E getArgumentInternal(String identifier, Class<E> argumentResultType) {
        Object argument = this.arguments.getArgumentValue(identifier);
        return argument == null ? null : argumentResultType.cast(argument);
    }

}
