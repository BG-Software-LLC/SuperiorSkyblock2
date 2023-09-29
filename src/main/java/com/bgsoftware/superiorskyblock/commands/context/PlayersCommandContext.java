package com.bgsoftware.superiorskyblock.commands.context;

import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.List;

public class PlayersCommandContext extends CommandContextImpl {

    private final List<SuperiorPlayer> players;

    public static PlayersCommandContext fromContext(CommandContext context, List<SuperiorPlayer> players) {
        CommandContextImpl contextImpl = (CommandContextImpl) context;
        return new PlayersCommandContext(contextImpl, players);
    }

    private PlayersCommandContext(CommandContextImpl context, List<SuperiorPlayer> players) {
        super(context);
        this.players = players;
    }

    public List<SuperiorPlayer> getPlayers() {
        return this.players;
    }

}
