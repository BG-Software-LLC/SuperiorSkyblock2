package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand2;
import com.bgsoftware.superiorskyblock.commands.context.CommandContextImpl;

public interface ISuperiorCommand<C extends CommandContext> extends SuperiorCommand2 {

    @Override
    default boolean displayCommand() {
        return true;
    }

    default void execute(SuperiorSkyblock plugin, CommandContext context) throws CommandSyntaxException {
        SuperiorSkyblockPlugin pluginInternal = (SuperiorSkyblockPlugin) plugin;
        execute(pluginInternal, createContext(pluginInternal, (CommandContextImpl) context));
    }

    C createContext(SuperiorSkyblockPlugin plugin, CommandContextImpl context) throws CommandSyntaxException;

    void execute(SuperiorSkyblockPlugin plugin, C context) throws CommandSyntaxException;

}
