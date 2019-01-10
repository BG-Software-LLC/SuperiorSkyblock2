package com.ome_r.superiorskyblock.commands.command;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.commands.ICommand;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.island.IslandPermission;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CmdBan implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("ban");
    }

    @Override
    public String getPermission() {
        return "superior.island.ban";
    }

    @Override
    public String getUsage() {
        return "island ban <player-name>";
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
        Island island = wrappedPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(wrappedPlayer);
            return;
        }

        if(!wrappedPlayer.hasPermission(IslandPermission.BAN_MEMBER)){
            Locale.NO_BAN_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.BAN_MEMBER));
            return;
        }

        WrappedPlayer targetPlayer = WrappedPlayer.of(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(wrappedPlayer, args[1]);
            return;
        }

        if(!targetPlayer.getIslandRole().isLessThan(wrappedPlayer.getIslandRole())) {
            Locale.BAN_PLAYERS_WITH_LOWER_ROLE.send(wrappedPlayer);
            return;
        }

        if(island.isBanned(targetPlayer)){
            Locale.PLAYER_ALREADY_BANNED.send(wrappedPlayer);
            return;
        }

        island.banMember(targetPlayer);

        for(UUID uuid : island.getAllMembers()){
            if(Bukkit.getOfflinePlayer(uuid).isOnline()){
                Locale.BAN_ANNOUNCEMENT.send(Bukkit.getPlayer(uuid), targetPlayer.getName(), wrappedPlayer.getName());
            }
        }

        Locale.GOT_BANNED.send(targetPlayer, island.getOwner().getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(args.length == 2 && island != null && wrappedPlayer.hasPermission(IslandPermission.BAN_MEMBER)){
            List<String> list = new ArrayList<>();
            WrappedPlayer targetPlayer;

            for(UUID uuid : island.getAllMembers()){
                targetPlayer = WrappedPlayer.of(uuid);
                if(targetPlayer.getIslandRole().isLessThan(wrappedPlayer.getIslandRole()) &&
                        targetPlayer.getName().toLowerCase().startsWith(args[1])){
                    list.add(targetPlayer.getName());
                }
            }

            return list;
        }

        return new ArrayList<>();
    }
}
