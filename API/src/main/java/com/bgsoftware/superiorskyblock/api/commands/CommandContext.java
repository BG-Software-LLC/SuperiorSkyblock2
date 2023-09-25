package com.bgsoftware.superiorskyblock.api.commands;

import org.bukkit.command.CommandSender;

import java.util.Optional;

public interface CommandContext {

    CommandSender getDispatcher();

    <E> Optional<E> getOptionalArgument(String identifier, Class<E> argumentResultType);

    <E> E getRequiredArgument(String identifier, Class<E> argumentResultType);

    String getInputArgument(String identifier);

    int getArgumentsCount();

}
