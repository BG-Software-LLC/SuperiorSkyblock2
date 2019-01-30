package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class CmdKick implements ICommand {

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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPermission.KICK_MEMBER)){
            Locale.NO_KICK_PERMISSION.send(superiorPlayer, island.getRequiredRole(IslandPermission.KICK_MEMBER));
            return;
        }

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

        if(targetPlayer == null || !island.isMember(targetPlayer)){
            Locale.PLAYER_NOT_INSIDE_ISLAND.send(superiorPlayer);
            return;
        }

        if(!targetPlayer.getIslandRole().isLessThan(superiorPlayer.getIslandRole())){
            Locale.KICK_PLAYERS_WITH_LOWER_ROLE.send(superiorPlayer);
            return;
        }

        island.kickMember(targetPlayer);

        for(UUID uuid : island.getAllMembers()){
            if(Bukkit.getOfflinePlayer(uuid).isOnline()){
                Locale.KICK_ANNOUNCEMENT.send(Bukkit.getPlayer(uuid), targetPlayer.getName(), superiorPlayer.getName());
            }
        }

        Locale.GOT_KICKED.send(targetPlayer, superiorPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPermission.KICK_MEMBER)){
            List<String> list = new ArrayList<>();
            SuperiorPlayer targetPlayer;

            for(UUID uuid : island.getMembers()){
                targetPlayer = SSuperiorPlayer.of(uuid);
                if(targetPlayer.getIslandRole().isLessThan(superiorPlayer.getIslandRole()) &&
                        targetPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())){
                    list.add(targetPlayer.getName());
                }
            }

            return list;
        }

        return new ArrayList<>();
    }
}
