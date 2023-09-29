package com.bgsoftware.superiorskyblock.api.commands.arguments;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

import java.util.Locale;

public interface CommandArgument<E> {

    String getIdentifier();

    String getDisplayName(Locale locale);

    boolean isOptional();

    CommandArgumentType<E> getType();

    static <E> CommandArgument<E> optional(String identifier, CommandArgumentType<E> argumentType) {
        return optional(identifier, identifier, argumentType);
    }

    static <E> CommandArgument<E> optional(String identifier, String displayName, CommandArgumentType<E> argumentType) {
        return SuperiorSkyblockAPI.getCommands().createArgument(identifier, argumentType, true, displayName);
    }

    static <E> CommandArgument<E> required(String identifier, CommandArgumentType<E> argumentType) {
        return required(identifier, identifier, argumentType);
    }

    static <E> CommandArgument<E> required(String identifier, String displayName, CommandArgumentType<E> argumentType) {
        return SuperiorSkyblockAPI.getCommands().createArgument(identifier, argumentType, false, displayName);
    }

}
