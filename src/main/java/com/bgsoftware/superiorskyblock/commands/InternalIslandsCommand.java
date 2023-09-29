package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.CommandContextImpl;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;

public interface InternalIslandsCommand extends ISuperiorCommand<IslandsCommandContext> {

    @Override
    default IslandsCommandContext createContext(SuperiorSkyblockPlugin plugin, CommandContextImpl context) {
        MultipleIslandsArgumentType.Result islandsResult = context.getRequiredArgument("islands", MultipleIslandsArgumentType.Result.class);
        return IslandsCommandContext.fromContext(context, islandsResult.getIslands(), islandsResult.getTargetPlayer());
    }

}
