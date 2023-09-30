package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class BoolArgumentType implements CommandArgumentType<Boolean> {

    public static final BoolArgumentType INSTANCE = new BoolArgumentType();

    private BoolArgumentType() {

    }

    @Override
    public Boolean parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        return Boolean.parseBoolean(reader.read());
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read().toLowerCase(Locale.ENGLISH);

        List<String> suggestions = new LinkedList<>();

        if ("true".contains(argument))
            suggestions.add("true");

        if ("false".contains(argument))
            suggestions.add("false");

        return suggestions;
    }

}
