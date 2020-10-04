package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminPlayerCommand;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminSetLeader implements IAdminPlayerCommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setleader");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setleader";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setleader <" +
                Locale.COMMAND_ARGUMENT_LEADER.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_NEW_LEADER.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_LEADER.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return 4;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultiplePlayers() {
        return false;
    }

    @Override
    public boolean requireIsland() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer leader, String[] args) {
        SuperiorPlayer newLeader = CommandArguments.getPlayer(plugin, sender, args[3]);

        if (newLeader == null)
            return;

        Island island = leader.getIsland();
        if (!island.getOwner().getUniqueId().equals(leader.getUniqueId())) {
            Locale.TRANSFER_ADMIN_NOT_LEADER.send(sender);
            return;
        }

        if (leader.getUniqueId().equals(newLeader.getUniqueId())) {
            Locale.TRANSFER_ADMIN_ALREADY_LEADER.send(sender, newLeader.getName());
            return;
        }

        if (!island.isMember(newLeader)) {
            Locale.TRANSFER_ADMIN_DIFFERENT_ISLAND.send(sender);
            return;
        }

        if(island.transferIsland(newLeader)) {
            Locale.TRANSFER_ADMIN.send(sender, leader.getName(), newLeader.getName());
            IslandUtils.sendMessage(island, Locale.TRANSFER_BROADCAST, new ArrayList<>(), newLeader.getName());
        }
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        Island playerIsland = targetPlayer.getIsland();
        return args.length != 4 ? new ArrayList<>() : CommandTabCompletes.getOnlinePlayers(plugin, args[2], false, onlinePlayer -> {
            Island onlineIsland = onlinePlayer.getIsland();
            return !onlinePlayer.equals(targetPlayer) && onlineIsland != null && !onlineIsland.equals(playerIsland) &&
                    onlineIsland.getOwner().equals(onlinePlayer);
        });
    }

}
