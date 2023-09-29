package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandRoleArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdSetRole implements InternalPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setrole");
    }

    @Override
    public String getPermission() {
        return "superior.island.setrole";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_SET_ROLE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("player", PlayerArgumentType.ALL_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .add(CommandArgument.required("island-role", IslandRoleArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_ISLAND_ROLE))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.SET_ROLE;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_SET_ROLE_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        SuperiorPlayer targetPlayer = context.getRequiredArgument("player", SuperiorPlayer.class);

        if (targetPlayer.getName().equals(dispatcher.getName())) {
            Message.SELF_ROLE_CHANGE.send(dispatcher);
            return;
        }

        PlayerRole playerRole = context.getRequiredArgument("island-role", PlayerRole.class);

        if (!playerRole.isRoleLadder()) {
            Message.INVALID_ROLE.send(dispatcher, context.getInputArgument("island-role"), SPlayerRole.getValuesString());
            return;
        }

        Island targetIsland = targetPlayer.getIsland();

        // Checking requirements for players
        if (dispatcher instanceof Player) {
            Island playerIsland = context.getIsland();

            if (!playerIsland.isMember(targetPlayer)) {
                Message.PLAYER_NOT_INSIDE_ISLAND.send(dispatcher);
                return;
            }

            targetIsland = playerIsland;

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(dispatcher);

            if (targetPlayer.getPlayerRole().isHigherThan(superiorPlayer.getPlayerRole()) ||
                    !playerRole.isLessThan(superiorPlayer.getPlayerRole())) {
                Message.CANNOT_SET_ROLE.send(dispatcher, playerRole);
                return;
            }
        } else {
            if (targetIsland == null) {
                Message.INVALID_ISLAND_OTHER.send(dispatcher, targetPlayer.getName());
                return;
            }

            if (playerRole.isLastRole()) {
                Message.CANNOT_SET_ROLE.send(dispatcher, playerRole);
                return;
            }
        }

        if (targetPlayer.getPlayerRole().equals(playerRole)) {
            Message.PLAYER_ALREADY_IN_ROLE.send(dispatcher, targetPlayer.getName(), playerRole);
            return;
        }

        int roleLimit = targetIsland.getRoleLimit(playerRole);

        if (roleLimit >= 0 && targetIsland.getIslandMembers(playerRole).size() >= roleLimit) {
            Message.CANNOT_SET_ROLE.send(dispatcher, playerRole);
            return;
        }

        PlayerRole currentRole = targetPlayer.getPlayerRole();

        if (!plugin.getEventsBus().callPlayerChangeRoleEvent(targetPlayer, playerRole))
            return;

        targetPlayer.setPlayerRole(playerRole);

        if (currentRole.isLessThan(playerRole)) {
            Message.PROMOTED_MEMBER.send(dispatcher, targetPlayer.getName(), targetPlayer.getPlayerRole());
            Message.GOT_PROMOTED.send(targetPlayer, targetPlayer.getPlayerRole());
        } else {
            Message.DEMOTED_MEMBER.send(dispatcher, targetPlayer.getName(), targetPlayer.getPlayerRole());
            Message.GOT_DEMOTED.send(targetPlayer, targetPlayer.getPlayerRole());
        }

    }

}
