package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.island.IslandPermission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CmdKick implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("kick", "remove");
    }

    @Override
    public String getPermission() {
        return "superior.island.kick";
    }

    @Override
    public String getUsage() {
        return "island kick <player-name>";
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

        if(!wrappedPlayer.hasPermission(IslandPermission.KICK_MEMBER)){
            Locale.NO_KICK_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.KICK_MEMBER));
            return;
        }

        WrappedPlayer targetPlayer = WrappedPlayer.of(args[1]);

        if(targetPlayer == null || !island.isMember(targetPlayer)){
            Locale.PLAYER_NOT_INSIDE_ISLAND.send(wrappedPlayer);
            return;
        }

        if(!targetPlayer.getIslandRole().isLessThan(wrappedPlayer.getIslandRole())){
            Locale.KICK_PLAYERS_WITH_LOWER_ROLE.send(wrappedPlayer);
            return;
        }

        island.kickMember(targetPlayer);

        for(UUID uuid : island.getAllMembers()){
            if(Bukkit.getOfflinePlayer(uuid).isOnline()){
                Locale.KICK_ANNOUNCEMENT.send(Bukkit.getPlayer(uuid), targetPlayer.getName(), wrappedPlayer.getName());
            }
        }

        Locale.GOT_KICKED.send(targetPlayer, wrappedPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(args.length == 2 && island != null && wrappedPlayer.hasPermission(IslandPermission.KICK_MEMBER)){
            List<String> list = new ArrayList<>();
            WrappedPlayer targetPlayer;

            for(UUID uuid : island.getMembers()){
                targetPlayer = WrappedPlayer.of(uuid);
                if(targetPlayer.getIslandRole().isLessThan(wrappedPlayer.getIslandRole()) &&
                        targetPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())){
                    list.add(targetPlayer.getName());
                }
            }

            return list;
        }

        return new ArrayList<>();
    }
}
