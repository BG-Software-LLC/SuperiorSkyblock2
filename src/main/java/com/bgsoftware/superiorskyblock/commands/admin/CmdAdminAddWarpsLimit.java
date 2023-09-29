package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IntArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminAddWarpsLimit implements InternalIslandsCommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addwarpslimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addwarpslimit";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_WARPS_LIMIT.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArguments.required("limit", IntArgumentType.LIMIT, Message.COMMAND_ARGUMENT_LIMIT))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandsCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        int limit = context.getRequiredArgument("limit", Integer.class);

        if (limit <= 0) {
            Message.INVALID_LIMIT.send(dispatcher);
            return;
        }

        List<Island> islands = context.getIslands();

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            EventResult<Integer> eventResult = plugin.getEventsBus().callIslandChangeWarpsLimitEvent(dispatcher,
                    island, island.getWarpsLimit() + limit);
            anyIslandChanged |= !eventResult.isCancelled();
            if (!eventResult.isCancelled())
                island.setWarpsLimit(eventResult.getResult());
        }

        if (!anyIslandChanged)
            return;

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() > 1)
            Message.CHANGED_WARPS_LIMIT_ALL.send(dispatcher);
        else if (targetPlayer == null)
            Message.CHANGED_WARPS_LIMIT_NAME.send(dispatcher, islands.get(0).getName());
        else
            Message.CHANGED_WARPS_LIMIT.send(dispatcher, targetPlayer.getName());
    }

}
