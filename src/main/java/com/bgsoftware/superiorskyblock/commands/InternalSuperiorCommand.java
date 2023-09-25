package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand2;

public interface InternalSuperiorCommand extends SuperiorCommand2 {

    @Override
    default boolean displayCommand() {
        return true;
    }

    default void execute(SuperiorSkyblock plugin, CommandContext context) {
        execute((SuperiorSkyblockPlugin) plugin, context);
    }

    void execute(SuperiorSkyblockPlugin plugin, CommandContext context);

}
