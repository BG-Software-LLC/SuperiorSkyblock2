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
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.util.Arrays;
import java.util.List;

public class CmdKick implements InternalPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("kick", "remove");
    }

    @Override
    public String getPermission() {
        return "superior.island.kick";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_KICK.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("player", PlayerArgumentType.ALL_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.KICK_MEMBER;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_KICK_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());
        Island island = context.getIsland();

        SuperiorPlayer targetPlayer = context.getRequiredArgument("player", SuperiorPlayer.class);

        if (!IslandUtils.checkKickRestrictions(superiorPlayer, island, targetPlayer))
            return;

        if (plugin.getSettings().isKickConfirm()) {
            plugin.getMenus().openConfirmKick(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island, targetPlayer);
        } else {
            IslandUtils.handleKickPlayer(superiorPlayer, island, targetPlayer);
        }
    }

}
