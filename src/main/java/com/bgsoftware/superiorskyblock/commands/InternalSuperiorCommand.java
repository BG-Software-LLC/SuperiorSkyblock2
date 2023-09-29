package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.commands.context.CommandContextImpl;

public interface InternalSuperiorCommand extends ISuperiorCommand<CommandContext> {

    @Override
    default CommandContext createContext(SuperiorSkyblockPlugin plugin, CommandContextImpl context) {
        return context;
    }

}
