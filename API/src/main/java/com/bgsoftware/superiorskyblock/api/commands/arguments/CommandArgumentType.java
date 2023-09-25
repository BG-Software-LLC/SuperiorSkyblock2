package com.bgsoftware.superiorskyblock.api.commands.arguments;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;

public interface CommandArgumentType<R, C extends CommandContext> {

    R parse(SuperiorSkyblock plugin, C context, ArgumentsReader reader) throws CommandSyntaxException;

}
