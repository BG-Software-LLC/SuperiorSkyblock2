package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.persistence.IPersistentDataHolder;
import com.bgsoftware.superiorskyblock.commands.context.CommandContextImpl;
import com.bgsoftware.superiorskyblock.commands.context.PersistentDataHolderCommandContext;

public interface InternalPersistentDataCommand extends ISuperiorCommand<PersistentDataHolderCommandContext> {

    @Override
    default PersistentDataHolderCommandContext createContext(SuperiorSkyblockPlugin plugin, CommandContextImpl context) {
        IPersistentDataHolder holder = context.getRequiredArgument("holder", IPersistentDataHolder.class);
        return PersistentDataHolderCommandContext.fromContext(context, holder);
    }

}
