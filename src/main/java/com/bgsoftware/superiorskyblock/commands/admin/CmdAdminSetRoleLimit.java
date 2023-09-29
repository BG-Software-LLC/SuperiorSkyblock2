package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IntArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandRoleArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetRoleLimit implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setrolelimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setrolelimit";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_ROLE_LIMIT.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArguments.required("island-role", IslandRoleArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_ISLAND_ROLE))
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

        PlayerRole playerRole = context.getRequiredArgument("island-role", PlayerRole.class);
        if (!IslandUtils.isValidRoleForLimit(playerRole)) {
            Message.INVALID_ROLE.send(dispatcher, context.getInputArgument("island-role"), SPlayerRole.getValuesString());
            return;
        }

        List<Island> islands = context.getIslands();
        int limit = context.getRequiredArgument("limit", Integer.class);

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            if (limit <= 0) {
                if (plugin.getEventsBus().callIslandRemoveRoleLimitEvent(dispatcher, island, playerRole)) {
                    anyIslandChanged = true;
                    island.removeRoleLimit(playerRole);
                }
            } else {
                EventResult<Integer> eventResult = plugin.getEventsBus().callIslandChangeRoleLimitEvent(
                        dispatcher, island, playerRole, limit);
                anyIslandChanged |= !eventResult.isCancelled();
                if (!eventResult.isCancelled())
                    island.setRoleLimit(playerRole, eventResult.getResult());
            }
        }

        if (!anyIslandChanged)
            return;

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() > 1)
            Message.CHANGED_ROLE_LIMIT_ALL.send(dispatcher, playerRole);
        else if (targetPlayer == null)
            Message.CHANGED_ROLE_LIMIT_NAME.send(dispatcher, playerRole, islands.get(0).getName());
        else
            Message.CHANGED_ROLE_LIMIT.send(dispatcher, playerRole, targetPlayer.getName());
    }

}
