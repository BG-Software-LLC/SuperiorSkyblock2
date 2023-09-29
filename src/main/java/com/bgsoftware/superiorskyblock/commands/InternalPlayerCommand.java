package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.context.CommandContextImpl;
import com.bgsoftware.superiorskyblock.commands.context.PlayerCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;

public interface InternalPlayerCommand extends ISuperiorCommand<PlayerCommandContext> {

    @Override
    default PlayerCommandContext createContext(SuperiorSkyblockPlugin plugin, CommandContextImpl context) throws CommandSyntaxException {
        SuperiorPlayer superiorPlayer = context.getRequiredArgument("player", SuperiorPlayer.class);

        if (requireIslandFromPlayer()) {
            Island playerIsland = superiorPlayer.getIsland();
            if (playerIsland == null) {
                Message.INVALID_ISLAND_OTHER.send(context.getDispatcher(), superiorPlayer.getName());
                throw new CommandSyntaxException("Invalid island");
            }
        }

        return PlayerCommandContext.fromContext(context, superiorPlayer);
    }

    boolean requireIslandFromPlayer();

}
