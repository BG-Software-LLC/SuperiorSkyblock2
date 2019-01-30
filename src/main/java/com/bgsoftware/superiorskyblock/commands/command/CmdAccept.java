package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.google.common.collect.Lists;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.island.IslandRole;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CmdAccept implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("accept", "join");
    }

    @Override
    public String getPermission() {
        return "superior.island.accept";
    }

    @Override
    public String getUsage() {
        return "island accept <player-name>";
    }

    @Override
    public int getMinArgs() {
        return 2;
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
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        WrappedPlayer targetPlayer = WrappedPlayer.of(args[1]);
        Island island;

        if(targetPlayer == null || !(island = plugin.getGrid().getIsland(targetPlayer)).isInvited(wrappedPlayer)){
            Locale.NO_ISLAND_INVITE.send(wrappedPlayer);
            return;
        }

        if(wrappedPlayer.getIsland() != null){
            Locale.JOIN_WHILE_IN_ISLAND.send(wrappedPlayer);
            return;
        }

        if(island.getTeamLimit() >= 0 && island.getAllMembers().size() >= island.getTeamLimit()){
            Locale.JOIN_FULL_ISLAND.send(wrappedPlayer);
            island.revokeInvite(wrappedPlayer);
            return;
        }

        for(UUID uuid : island.getAllMembers()){
            if(Bukkit.getOfflinePlayer(uuid).isOnline()){
                Locale.JOIN_ANNOUNCEMENT.send(Bukkit.getPlayer(uuid),  wrappedPlayer.getName());
            }
        }

        island.revokeInvite(wrappedPlayer);
        island.addMember(wrappedPlayer, IslandRole.MEMBER);

        Locale.JOINED_ISLAND.send(wrappedPlayer, targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if(args.length == 2){
            WrappedPlayer wrappedPlayer = WrappedPlayer.of((Player) sender);
            List<String> list = Lists.newArrayList();
            Island island;

            for(UUID uuid : plugin.getGrid().getAllIslands()){
                island = plugin.getGrid().getIsland(WrappedPlayer.of(uuid));
                if(island.isInvited(wrappedPlayer) && wrappedPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                    list.add(WrappedPlayer.of(uuid).getName());
            }

            return list;
        }

        return Lists.newArrayList();
    }
}
