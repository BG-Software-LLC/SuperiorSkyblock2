package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;

public class BoolArgumentType implements CommandArgumentType<Boolean> {

    public static final BoolArgumentType INSTANCE = new BoolArgumentType();

    private BoolArgumentType() {

    }

    @Override
    public Boolean parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        return Boolean.parseBoolean(reader.read());
    }

}
