package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdPardon implements InternalPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("pardon", "unban");
    }

    @Override
    public String getPermission() {
        return "superior.island.pardon";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_PARDON.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("player", PlayerArgumentType.ALL_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.BAN_MEMBER;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_BAN_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());
        Island island = context.getIsland();

        SuperiorPlayer targetPlayer = context.getRequiredArgument("player", SuperiorPlayer.class);

        if (!island.isBanned(targetPlayer)) {
            Message.PLAYER_NOT_BANNED.send(superiorPlayer);
            return;
        }

        if (!plugin.getEventsBus().callIslandUnbanEvent(superiorPlayer, targetPlayer, island))
            return;

        island.unbanMember(targetPlayer);

        IslandUtils.sendMessage(island, Message.UNBAN_ANNOUNCEMENT, Collections.emptyList(), targetPlayer.getName(), superiorPlayer.getName());

        Message.GOT_UNBANNED.send(targetPlayer, island.getOwner().getName());
    }

}
