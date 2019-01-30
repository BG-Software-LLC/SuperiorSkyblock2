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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmdSetPermission implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setpermission", "setperm");
    }

    @Override
    public String getPermission() {
        return "superior.island.setpermission";
    }

    @Override
    public String getUsage() {
        return "island setpermission <island-role> <island-permission> <true/false>";
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

        if(!wrappedPlayer.hasPermission(IslandPermission.SET_PERMISSION)){
            Locale.NO_PERMISSION_SET_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.SET_PERMISSION));
            return;
        }

        IslandRole islandRole;
        IslandPermission islandPermission;
        boolean value;

        try{
            islandRole = IslandRole.valueOf(args[1].toUpperCase());
        }catch(IllegalArgumentException ex){
            Locale.INVALID_ROLE.send(wrappedPlayer, args[1], IslandRole.getValuesString());
            return;
        }

        if(!islandRole.isLessThan(wrappedPlayer.getIslandRole())){
            Locale.CHANGE_PERMISSION_FOR_HIGHER_ROLE.send(wrappedPlayer);
            return;
        }

        try{
            islandPermission = IslandPermission.valueOf(args[2].toUpperCase());
        }catch(IllegalArgumentException ex){
            Locale.INVALID_ISLAND_PERMISSION.send(wrappedPlayer, args[2], IslandPermission.getValuesString());
            return;
        }

        if(!wrappedPlayer.hasPermission(islandPermission)){
            Locale.LACK_CHANGE_PERMISSION.send(wrappedPlayer);
            return;
        }



        try{
            value = Boolean.parseBoolean(args[3]);
        }catch(IllegalArgumentException ex){
            Locale.INVALID_BOOLEAN.send(wrappedPlayer);
            return;
        }

        island.setPermission(islandRole, islandPermission, value);
        Locale.UPDATED_PERMISSION.send(wrappedPlayer, islandRole);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(island != null && wrappedPlayer.hasPermission(IslandPermission.SET_PERMISSION)){
            List<String> list = new ArrayList<>();

            if (args.length == 2) {
                for(IslandRole islandRole : IslandRole.values()) {
                    if(islandRole.name().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(islandRole.name().toLowerCase());
                }
            }
            else if (args.length == 3) {
                for(IslandPermission islandPermission : IslandPermission.values()) {
                    if(islandPermission.name().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(islandPermission.name().toLowerCase());
                }
            }
            else if(args.length == 4){
                list.addAll(Stream.of("true", "false")
                        .filter(value -> value.startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList()));
            }

            return list;
        }

        return new ArrayList<>();
    }
}
