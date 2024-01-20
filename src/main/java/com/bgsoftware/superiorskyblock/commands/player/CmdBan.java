package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.util.Collections;
import java.util.List;

public class CmdBan implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("ban");
    }

    @Override
    public String getPermission() {
        return "superior.island.ban";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "ban [" + Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_BAN.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 2;
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
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        if (args.length == 1) {
            plugin.getMenus().openIslandBannedPlayers(superiorPlayer,
                    MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
            return;
        }

        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, superiorPlayer, args[1]);

        if (targetPlayer == null)
            return;

        if (!IslandUtils.checkBanRestrictions(superiorPlayer, island, targetPlayer))
            return;

        if (plugin.getSettings().isBanConfirm()) {
            plugin.getMenus().openConfirmBan(superiorPlayer, null, island, targetPlayer);
        } else {
            IslandUtils.handleBanPlayer(superiorPlayer, island, targetPlayer);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length != 2 ? Collections.emptyList() : CommandTabCompletes.getOnlinePlayers(plugin, args[1], true,
                onlinePlayer -> !island.isBanned(onlinePlayer) && (!island.isMember(onlinePlayer) ||
                        onlinePlayer.getPlayerRole().isLessThan(superiorPlayer.getPlayerRole())));
    }

}
