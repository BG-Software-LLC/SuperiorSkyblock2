package com.bgsoftware.superiorskyblock.commands.context;

import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.persistence.IPersistentDataHolder;

public class PersistentDataHolderCommandContext extends CommandContextImpl {

    private final IPersistentDataHolder persistentDataHolder;

    public static PersistentDataHolderCommandContext fromContext(CommandContext context, IPersistentDataHolder persistentDataHolder) {
        CommandContextImpl contextImpl = (CommandContextImpl) context;
        return new PersistentDataHolderCommandContext(contextImpl, persistentDataHolder);
    }

    private PersistentDataHolderCommandContext(CommandContextImpl context, IPersistentDataHolder persistentDataHolder) {
        super(context);
        this.persistentDataHolder = persistentDataHolder;
    }

    public IPersistentDataHolder getPersistentDataHolder() {
        return persistentDataHolder;
    }

}
