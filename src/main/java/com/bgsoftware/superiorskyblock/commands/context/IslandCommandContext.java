package com.bgsoftware.superiorskyblock.commands.context;

import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.island.Island;

public class IslandCommandContext extends CommandContextImpl {

    private final Island island;

    public static IslandCommandContext fromContext(CommandContext context, Island island) {
        CommandContextImpl contextImpl = (CommandContextImpl) context;
        return new IslandCommandContext(contextImpl, island);
    }

    private IslandCommandContext(CommandContextImpl context, Island island) {
        super(context);
        this.island = island;
    }

    public Island getIsland() {
        return island;
    }

}
