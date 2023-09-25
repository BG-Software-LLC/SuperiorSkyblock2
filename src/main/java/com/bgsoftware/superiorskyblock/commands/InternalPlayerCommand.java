package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.context.PlayerCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;

public interface InternalPlayerCommand extends InternalSuperiorCommand {

    @Override
    default void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        SuperiorPlayer superiorPlayer = context.getRequiredArgument("player", SuperiorPlayer.class);

        if (requireIsland()) {
            Island playerIsland = superiorPlayer.getIsland();
            if (playerIsland == null) {
                Message.INVALID_ISLAND_OTHER.send(context.getDispatcher(), superiorPlayer.getName());
                return;
            }
        }

        execute(plugin, PlayerCommandContext.fromContext(context, superiorPlayer));
    }

    default boolean requireIsland() {
        return false;
    }

    void execute(SuperiorSkyblockPlugin plugin, PlayerCommandContext context);

}
