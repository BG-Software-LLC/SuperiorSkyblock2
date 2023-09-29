package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPlayerCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.PlayerCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetLeader implements InternalPlayerCommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setleader");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setleader";
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("player", PlayerArgumentType.ALL_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .add(CommandArguments.required("new-leader", PlayerArgumentType.ALL_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .build();
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_LEADER.getMessage(locale);
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean requireIslandFromPlayer() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, PlayerCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        SuperiorPlayer leader = context.getSuperiorPlayer();
        SuperiorPlayer newLeader = context.getRequiredArgument("new-leader", SuperiorPlayer.class);

        Island island = leader.getIsland();

        if (!island.getOwner().getUniqueId().equals(leader.getUniqueId())) {
            Message.TRANSFER_ADMIN_NOT_LEADER.send(dispatcher);
            return;
        }

        if (leader.getUniqueId().equals(newLeader.getUniqueId())) {
            Message.TRANSFER_ADMIN_ALREADY_LEADER.send(dispatcher, newLeader.getName());
            return;
        }

        if (!island.isMember(newLeader)) {
            Message.TRANSFER_ADMIN_DIFFERENT_ISLAND.send(dispatcher);
            return;
        }

        if (island.transferIsland(newLeader)) {
            Message.TRANSFER_ADMIN.send(dispatcher, leader.getName(), newLeader.getName());
            IslandUtils.sendMessage(island, Message.TRANSFER_BROADCAST, Collections.emptyList(), newLeader.getName());
        }
    }

}
