package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Comparator;
import java.util.Locale;

public class IslandPrivilegeArgumentType implements CommandArgumentType<IslandPrivilege> {

    public static final IslandPrivilegeArgumentType INSTANCE = new IslandPrivilegeArgumentType();

    @Override
    public IslandPrivilege parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        IslandPrivilege islandPrivilege = null;

        try {
            islandPrivilege = IslandPrivilege.getByName(argument);
        } catch (NullPointerException ignored) {
        }

        if (islandPrivilege == null) {
            Message.INVALID_ISLAND_PERMISSION.send(context.getDispatcher(), argument, Formatters.COMMA_FORMATTER.format(
                    IslandPrivilege.values().stream()
                            .sorted(Comparator.comparing(IslandPrivilege::getName))
                            .map(_islandPrivilege -> _islandPrivilege.toString().toLowerCase(Locale.ENGLISH))));
            throw new CommandSyntaxException("Invalid privilege: " + argument);
        }

        return islandPrivilege;
    }

}
