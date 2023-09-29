package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPlayerCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.PlayerCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminPromote implements InternalPlayerCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("promote");
    }

    @Override
    public String getPermission() {
        return "superior.admin.promote";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_PROMOTE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("player", PlayerArgumentType.ALL_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .build();
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

        SuperiorPlayer targetPlayer = context.getSuperiorPlayer();

        PlayerRole currentRole = targetPlayer.getPlayerRole();

        if (currentRole.isLastRole()) {
            Message.LAST_ROLE_PROMOTE.send(dispatcher);
            return;
        }

        Island island = targetPlayer.getIsland();

        PlayerRole nextRole = currentRole;
        int roleLimit;

        do {
            nextRole = nextRole.getNextRole();
            roleLimit = nextRole == null ? -1 : island.getRoleLimit(nextRole);
        } while (nextRole != null && !nextRole.isLastRole() &&
                roleLimit >= 0 && island.getIslandMembers(nextRole).size() >= roleLimit);

        if (nextRole == null || nextRole.isLastRole()) {
            Message.LAST_ROLE_PROMOTE.send(dispatcher);
            return;
        }

        if (!plugin.getEventsBus().callPlayerChangeRoleEvent(targetPlayer, nextRole))
            return;

        targetPlayer.setPlayerRole(nextRole);

        Message.PROMOTED_MEMBER.send(dispatcher, targetPlayer.getName(), targetPlayer.getPlayerRole());
        Message.GOT_PROMOTED.send(targetPlayer, targetPlayer.getPlayerRole());
    }

}
