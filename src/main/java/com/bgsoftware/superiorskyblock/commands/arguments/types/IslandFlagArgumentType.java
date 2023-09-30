package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class IslandFlagArgumentType implements CommandArgumentType<IslandFlag> {

    public static final IslandFlagArgumentType INSTANCE = new IslandFlagArgumentType();

    @Override
    public IslandFlag parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        IslandFlag islandFlag = null;

        try {
            islandFlag = IslandFlag.getByName(argument);
        } catch (NullPointerException ignored) {
        }

        if (islandFlag == null) {
            Message.INVALID_SETTINGS.send(context.getDispatcher(), argument, Formatters.COMMA_FORMATTER.format(IslandFlag.values().stream()
                    .sorted(Comparator.comparing(IslandFlag::getName))
                    .map(_islandFlag -> _islandFlag.getName().toLowerCase(Locale.ENGLISH))));
            throw new CommandSyntaxException("Invalid island flag");
        }

        return islandFlag;
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read().toLowerCase(Locale.ENGLISH);

        List<String> suggestions = new LinkedList<>();

        for (IslandFlag islandFlag : IslandFlag.values()) {
            String islandFlagName = islandFlag.getName().toLowerCase(Locale.ENGLISH);
            if (islandFlagName.contains(argument))
                suggestions.add(islandFlagName);
        }

        return suggestions;
    }

}
