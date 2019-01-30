package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.google.common.collect.Lists;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class CmdAccept implements ICommand {

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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);
        Island island;

        if(targetPlayer == null || !(island = plugin.getGrid().getIsland(targetPlayer)).isInvited(superiorPlayer)){
            Locale.NO_ISLAND_INVITE.send(superiorPlayer);
            return;
        }

        if(superiorPlayer.getIsland() != null){
            Locale.JOIN_WHILE_IN_ISLAND.send(superiorPlayer);
            return;
        }

        if(island.getTeamLimit() >= 0 && island.getAllMembers().size() >= island.getTeamLimit()){
            Locale.JOIN_FULL_ISLAND.send(superiorPlayer);
            island.revokeInvite(superiorPlayer);
            return;
        }

        for(UUID uuid : island.getAllMembers()){
            if(Bukkit.getOfflinePlayer(uuid).isOnline()){
                Locale.JOIN_ANNOUNCEMENT.send(Bukkit.getPlayer(uuid),  superiorPlayer.getName());
            }
        }

        island.revokeInvite(superiorPlayer);
        island.addMember(superiorPlayer, IslandRole.MEMBER);

        Locale.JOINED_ISLAND.send(superiorPlayer, targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if(args.length == 2){
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of((Player) sender);
            List<String> list = Lists.newArrayList();
            Island island;

            for(UUID uuid : plugin.getGrid().getAllIslands()){
                island = plugin.getGrid().getIsland(SSuperiorPlayer.of(uuid));
                if(island.isInvited(superiorPlayer) && superiorPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                    list.add(SSuperiorPlayer.of(uuid).getName());
            }

            return list;
        }

        return Lists.newArrayList();
    }
}
