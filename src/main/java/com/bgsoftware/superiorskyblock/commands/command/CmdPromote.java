package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.island.IslandPermission;
import com.bgsoftware.superiorskyblock.island.IslandRole;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CmdPromote implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("promote");
    }

    @Override
    public String getPermission() {
        return "superior.island.promote";
    }

    @Override
    public String getUsage() {
        return "island promote <player-name>";
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

        if(!wrappedPlayer.hasPermission(IslandPermission.PROMOTE_MEMBERS)){
            Locale.NO_PROMOTE_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.PROMOTE_MEMBERS));
            return;
        }

        WrappedPlayer targetPlayer = WrappedPlayer.of(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(wrappedPlayer, args[1]);
            return;
        }

        if(!targetPlayer.getIslandRole().isLessThan(wrappedPlayer.getIslandRole()) ||
                targetPlayer.getIslandRole().getNextRole().isHigherThan(wrappedPlayer.getIslandRole())){
            Locale.PROMOTE_PLAYERS_WITH_LOWER_ROLE.send(wrappedPlayer);
            return;
        }

        if(targetPlayer.getIslandRole().getNextRole() == IslandRole.LEADER){
            Locale.LAST_ROLE_PROMOTE.send(wrappedPlayer);
            return;
        }

        targetPlayer.setIslandRole(targetPlayer.getIslandRole().getNextRole());

        Locale.PROMOTED_MEMBER.send(wrappedPlayer, targetPlayer.getName(), targetPlayer.getIslandRole());
        Locale.GOT_PROMOTED.send(targetPlayer, targetPlayer.getIslandRole());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(args.length == 2 && island != null && wrappedPlayer.hasPermission(IslandPermission.PROMOTE_MEMBERS)){
            List<String> list = new ArrayList<>();
            WrappedPlayer targetPlayer;

            for(UUID uuid : island.getAllMembers()){
                targetPlayer = WrappedPlayer.of(uuid);
                if(targetPlayer.getIslandRole().isLessThan(wrappedPlayer.getIslandRole()) &&
                        !targetPlayer.getIslandRole().getNextRole().isHigherThan(wrappedPlayer.getIslandRole()) &&
                        targetPlayer.getIslandRole().getNextRole() != IslandRole.LEADER &&
                        targetPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())){
                    list.add(targetPlayer.getName());
                }
            }

            return list;
        }

        return new ArrayList<>();
    }
}
