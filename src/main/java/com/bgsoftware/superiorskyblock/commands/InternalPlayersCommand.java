package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.context.CommandContextImpl;
import com.bgsoftware.superiorskyblock.commands.context.PlayersCommandContext;

import java.util.List;

public interface InternalPlayersCommand extends ISuperiorCommand<PlayersCommandContext> {

    @Override
    default PlayersCommandContext createContext(SuperiorSkyblockPlugin plugin, CommandContextImpl context) {
        List<SuperiorPlayer> targetPlayers = context.getRequiredArgument("players", List.class);
        return PlayersCommandContext.fromContext(context, targetPlayers);
    }

}
