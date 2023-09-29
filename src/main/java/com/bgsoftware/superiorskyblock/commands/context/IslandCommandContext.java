package com.bgsoftware.superiorskyblock.commands.context;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public class IslandCommandContext extends CommandContextImpl {

    private final Island island;
    @Nullable
    private final SuperiorPlayer targetPlayer;

    public static IslandCommandContext fromContext(CommandContext context, Island island, @Nullable SuperiorPlayer targetPlayer) {
        CommandContextImpl contextImpl = (CommandContextImpl) context;
        return new IslandCommandContext(contextImpl, island, targetPlayer);
    }

    private IslandCommandContext(CommandContextImpl context, Island island, @Nullable SuperiorPlayer targetPlayer) {
        super(context);
        this.island = island;
        this.targetPlayer = targetPlayer;
    }

    public Island getIsland() {
        return island;
    }

    @Nullable
    public SuperiorPlayer getTargetPlayer() {
        return targetPlayer;
    }

}
