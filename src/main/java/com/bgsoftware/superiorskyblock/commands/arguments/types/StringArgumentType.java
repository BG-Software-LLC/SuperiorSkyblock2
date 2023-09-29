package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;

public class StringArgumentType implements CommandArgumentType<String> {

    public static final StringArgumentType INSTANCE = new StringArgumentType(false, false);
    public static final StringArgumentType MULTIPLE = new StringArgumentType(true, false);
    public static final StringArgumentType MULTIPLE_COLORIZE = new StringArgumentType(true, true);


    private final boolean isMultiple;
    private final boolean isColorize;

    private StringArgumentType(boolean isMultiple, boolean isColorize) {
        this.isMultiple = isMultiple;
        this.isColorize = isColorize;
    }

    @Override
    public String parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        StringBuilder argument = new StringBuilder(reader.read());

        while (this.isMultiple && reader.hasNext())
            argument.append(" ").append(reader.read());

        return this.isColorize ? Formatters.COLOR_FORMATTER.format(argument.toString()) : argument.toString();
    }

}
