package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetLeader implements ICommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setleader");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setleader";
    }

    @Override
    public String getUsage() {
        return "island admin setleader <leader> <new leader>";
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer leader = SSuperiorPlayer.of(args[2]);
        SuperiorPlayer newLeader = SSuperiorPlayer.of(args[3]);


        if (leader == null) {
            Locale.INVALID_PLAYER.send(sender, args[2]);
            return;
        }

        if (newLeader == null) {
            Locale.INVALID_PLAYER.send(sender, args[3]);
            return;
        }

        Island island = leader.getIsland();
        if (island == null || !island.getOwner().getUniqueId().equals(leader.getUniqueId())) {
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

        island.transfer(newLeader);
        Locale.TRANSFER_ADMIN.send(sender, leader.getName(), newLeader.getName());
        island.sendMessage(Locale.TRANSFER_BROADCAST.getMessage(newLeader.getName()));

    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
