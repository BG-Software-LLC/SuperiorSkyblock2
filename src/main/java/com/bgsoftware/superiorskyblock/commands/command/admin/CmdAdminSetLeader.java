package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_LEADER.getMessage();
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
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                if(!player.equals(sender) && player.getName().toLowerCase().startsWith(args[2].toLowerCase()) &&
                        SSuperiorPlayer.of(player).getIsland() != null){
                    list.add(player.getName());
                }
            }
        }
        else if(args.length == 4){
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(args[2]);
            if(superiorPlayer != null && superiorPlayer.getIsland() != null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.equals(sender) && !player.getUniqueId().equals(superiorPlayer.getUniqueId()) &&
                            player.getName().toLowerCase().startsWith(args[3].toLowerCase()) &&
                            superiorPlayer.getIsland().equals(SSuperiorPlayer.of(player).getIsland())){
                        list.add(player.getName());
                    }
                }
            }
        }

        return list;
    }
}
