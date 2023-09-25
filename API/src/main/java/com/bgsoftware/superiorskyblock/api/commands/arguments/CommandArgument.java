package com.bgsoftware.superiorskyblock.api.commands.arguments;

import com.avaje.ebeaninternal.server.core.Message;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

public interface CommandArgument<E> {

    String getIdentifier();

    String getDisplayName();

    boolean isOptional();

    CommandArgumentType<E> getType();

    static <E> CommandArgument<E> optional(String identifier, CommandArgumentType<E> argumentType) {
        return optional(identifier, identifier, argumentType);
    }

    static <E> CommandArgument<E> optional(String identifier, String displayName, CommandArgumentType<E> argumentType) {
        return SuperiorSkyblockAPI.getCommands().createArgument(identifier, displayName, argumentType, false);
    }

    static <E> CommandArgument<E> required(String identifier, CommandArgumentType<E> argumentType) {
        return required(identifier, identifier, argumentType);
    }

    static <E> CommandArgument<E> required(String identifier, String displayName, CommandArgumentType<E> argumentType) {
        return SuperiorSkyblockAPI.getCommands().createArgument(identifier, displayName, argumentType, true);
    }

}
