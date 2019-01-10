package com.ome_r.superiorskyblock.commands.command;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.commands.ICommand;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.island.IslandPermission;
import com.ome_r.superiorskyblock.island.IslandRole;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CmdSetRole implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setrole");
    }

    @Override
    public String getPermission() {
        return "superior.island.setrole";
    }

    @Override
    public String getUsage() {
        return "island setrole <player-name> <island-role>";
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer targetPlayer = WrappedPlayer.of(args[1]);
        IslandRole islandRole;

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[1]);
            return;
        }

        if(targetPlayer.getName().equals(sender.getName())){
            Locale.SELF_ROLE_CHANGE.send(sender);
            return;
        }

        try{
            islandRole = IslandRole.valueOf(args[2].toUpperCase());
        }catch(IllegalArgumentException ex){
            Locale.INVALID_ROLE.send(sender, args[2], IslandRole.getValuesString());
            return;
        }

        Island island = targetPlayer.getIsland();

        if(sender instanceof Player){
            WrappedPlayer wrappedPlayer = WrappedPlayer.of((Player) sender);
            island = wrappedPlayer.getIsland();

            if(island == null){
                Locale.INVALID_ISLAND.send(wrappedPlayer);
                return;
            }

            if(!wrappedPlayer.hasPermission(IslandPermission.SET_ROLE)){
                Locale.NO_SET_ROLE_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.SET_ROLE));
                return;
            }

            if(!island.isMember(targetPlayer)){
                Locale.PLAYER_NOT_INSIDE_ISLAND.send(sender);
                return;
            }

            if(!islandRole.isLessThan(wrappedPlayer.getIslandRole()) || islandRole == IslandRole.GUEST){
                Locale.CANNOT_SET_ROLE.send(sender, islandRole);
                return;
            }
        }else {
            if (island == null) {
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
                return;
            }

            if(islandRole == IslandRole.LEADER || islandRole == IslandRole.GUEST){
                Locale.CANNOT_SET_ROLE.send(sender, islandRole);
                return;
            }
        }

        if(targetPlayer.getIslandRole() == islandRole){
            Locale.PLAYER_ALREADY_IN_ROLE.send(sender, targetPlayer.getName(), islandRole);
            return;
        }

        IslandRole currentRole = targetPlayer.getIslandRole();
        targetPlayer.setIslandRole(islandRole);

        if(currentRole.isLessThan(islandRole)){
            Locale.PROMOTED_MEMBER.send(sender, targetPlayer.getName(), targetPlayer.getIslandRole());
            Locale.GOT_PROMOTED.send(targetPlayer, targetPlayer.getIslandRole());
        }else{
            Locale.DEMOTED_MEMBER.send(sender, targetPlayer.getName(), targetPlayer.getIslandRole());
            Locale.GOT_DEMOTED.send(targetPlayer, targetPlayer.getIslandRole());
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(island != null && wrappedPlayer.hasPermission(IslandPermission.SET_ROLE)){
            List<String> list = new ArrayList<>();
            WrappedPlayer targetPlayer;

            if(args.length == 2) {
                for(UUID uuid : island.getAllMembers()){
                    targetPlayer = WrappedPlayer.of(uuid);
                    if(targetPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(targetPlayer.getName());
                }
            }
            else if(args.length == 3){
                for(IslandRole islandRole : IslandRole.values()) {
                    if(islandRole.name().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(islandRole.name().toLowerCase());
                }
            }

            return list;
        }

        return new ArrayList<>();
    }
}
