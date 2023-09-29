package com.bgsoftware.superiorskyblock.api.commands.arguments;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

import java.util.Locale;

public interface CommandArgument<E> {

    String getIdentifier();

    String getDisplayName(Locale locale);

    boolean isOptional();

    CommandArgumentType<E> getType();

    static <E> CommandArgument<E> optional(String identifier, CommandArgumentType<E> argumentType, Object... displayNameComponents) {
        return SuperiorSkyblockAPI.getCommands().createArgument(identifier, argumentType, true, displayNameComponents);
    }

    static <E> CommandArgument<E> required(String identifier, CommandArgumentType<E> argumentType, Object... displayNameComponents) {
        return SuperiorSkyblockAPI.getCommands().createArgument(identifier, argumentType, false, displayNameComponents);
    }

}
