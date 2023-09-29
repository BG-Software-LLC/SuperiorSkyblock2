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

public class CmdCoop implements InternalPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("coop", "trust");
    }

    @Override
    public String getPermission() {
        return "superior.island.coop";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_COOP.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("player", PlayerArgumentType.ONLINE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.COOP_MEMBER;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_COOP_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());
        Island island = context.getIsland();

        SuperiorPlayer targetPlayer = context.getRequiredArgument("player", SuperiorPlayer.class);

        if (island.isMember(targetPlayer)) {
            Message.ALREADY_IN_ISLAND_OTHER.send(superiorPlayer);
            return;
        }

        if (island.isCoop(targetPlayer)) {
            Message.PLAYER_ALREADY_COOP.send(superiorPlayer);
            return;
        }

        if (island.isBanned(targetPlayer)) {
            Message.COOP_BANNED_PLAYER.send(superiorPlayer);
            return;
        }

        if (island.getCoopPlayers().size() >= island.getCoopLimit()) {
            Message.COOP_LIMIT_EXCEED.send(superiorPlayer);
            return;
        }

        if (!plugin.getEventsBus().callIslandCoopPlayerEvent(island, superiorPlayer, targetPlayer))
            return;

        island.addCoop(targetPlayer);

        IslandUtils.sendMessage(island, Message.COOP_ANNOUNCEMENT, Collections.emptyList(), superiorPlayer.getName(), targetPlayer.getName());

        if (island.getName().isEmpty())
            Message.JOINED_ISLAND_AS_COOP.send(targetPlayer, superiorPlayer.getName());
        else
            Message.JOINED_ISLAND_AS_COOP_NAME.send(targetPlayer, island.getName());
    }

}
