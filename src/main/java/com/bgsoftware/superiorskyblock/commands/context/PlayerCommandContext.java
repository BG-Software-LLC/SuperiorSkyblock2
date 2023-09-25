package com.bgsoftware.superiorskyblock.commands.context;

import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public class PlayerCommandContext extends CommandContextImpl {

    private final SuperiorPlayer superiorPlayer;

    public static PlayerCommandContext fromContext(CommandContext context, SuperiorPlayer superiorPlayer) {
        CommandContextImpl contextImpl = (CommandContextImpl) context;
        return new PlayerCommandContext(contextImpl, superiorPlayer);
    }

    private PlayerCommandContext(CommandContextImpl context, SuperiorPlayer superiorPlayer) {
        super(context);
        this.superiorPlayer = superiorPlayer;
    }

    public SuperiorPlayer getSuperiorPlayer() {
        return this.superiorPlayer;
    }

}
