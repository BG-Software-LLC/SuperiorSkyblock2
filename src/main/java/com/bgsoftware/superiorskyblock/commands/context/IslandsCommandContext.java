package com.bgsoftware.superiorskyblock.commands.context;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.List;

public class IslandsCommandContext extends CommandContextImpl {

    private final List<Island> islands;
    @Nullable
    private final SuperiorPlayer targetPlayer;

    public static IslandsCommandContext fromContext(CommandContext context, List<Island> islands, @Nullable SuperiorPlayer targetPlayer) {
        CommandContextImpl contextImpl = (CommandContextImpl) context;
        return new IslandsCommandContext(contextImpl, islands, targetPlayer);
    }

    private IslandsCommandContext(CommandContextImpl context, List<Island> islands, @Nullable SuperiorPlayer targetPlayer) {
        super(context);
        this.islands = islands;
        this.targetPlayer = targetPlayer;
    }

    public List<Island> getIslands() {
        return this.islands;
    }

    @Nullable
    public SuperiorPlayer getTargetPlayer() {
        return targetPlayer;
    }

}
