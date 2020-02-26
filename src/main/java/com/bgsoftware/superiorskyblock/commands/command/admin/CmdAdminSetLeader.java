package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminSetLeader implements ISuperiorCommand {
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

        if(island.transferIsland(newLeader)) {
            Locale.TRANSFER_ADMIN.send(sender, leader.getName(), newLeader.getName());
            ((SIsland) island).sendMessage(Locale.TRANSFER_BROADCAST, new ArrayList<>(), newLeader.getName());
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }

        else if(args.length == 4){
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(args[2]);
            if(superiorPlayer != null && superiorPlayer.getIsland() != null) {
                Island playerIsland = superiorPlayer.getIsland();
                for(Player player : Bukkit.getOnlinePlayers()){
                    SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                    if(!onlinePlayer.equals(superiorPlayer)) {
                        Island onlineIsland = onlinePlayer.getIsland();
                        if (onlineIsland != null && !onlineIsland.equals(playerIsland)) {
                            if (player.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                                list.add(player.getName());
                            if (!onlineIsland.getName().isEmpty() && onlineIsland.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                                list.add(onlineIsland.getName());
                        }
                    }
                }
            }
        }

        return list;
    }
}
