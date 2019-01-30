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

public class CmdDemote implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("demote");
    }

    @Override
    public String getPermission() {
        return "superior.island.demote";
    }

    @Override
    public String getUsage() {
        return "island demote <player-name>";
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

        if(!wrappedPlayer.hasPermission(IslandPermission.DEMOTE_MEMBERS)){
            Locale.NO_DEMOTE_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.DEMOTE_MEMBERS));
            return;
        }

        WrappedPlayer targetPlayer = WrappedPlayer.of(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(wrappedPlayer, args[1]);
            return;
        }

        if(!targetPlayer.getIslandRole().isLessThan(wrappedPlayer.getIslandRole())){
            Locale.DEMOTE_PLAYERS_WITH_LOWER_ROLE.send(wrappedPlayer);
            return;
        }

        if(targetPlayer.getIslandRole().getPreviousRole() == IslandRole.GUEST){
            Locale.LAST_ROLE_DEMOTE.send(wrappedPlayer);
            return;
        }

        targetPlayer.setIslandRole(targetPlayer.getIslandRole().getPreviousRole());

        Locale.DEMOTED_MEMBER.send(wrappedPlayer, targetPlayer.getName(), targetPlayer.getIslandRole());
        Locale.GOT_DEMOTED.send(targetPlayer, targetPlayer.getIslandRole());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(args.length == 2 && island != null && wrappedPlayer.hasPermission(IslandPermission.DEMOTE_MEMBERS)){
            List<String> list = new ArrayList<>();
            WrappedPlayer targetPlayer;

            for(UUID uuid : island.getAllMembers()){
                targetPlayer = WrappedPlayer.of(uuid);
                if(targetPlayer.getIslandRole().isLessThan(wrappedPlayer.getIslandRole()) &&
                        targetPlayer.getIslandRole().getPreviousRole() != IslandRole.GUEST &&
                        targetPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())){
                    list.add(targetPlayer.getName());
                }
            }

            return list;
        }

        return new ArrayList<>();
    }
}
