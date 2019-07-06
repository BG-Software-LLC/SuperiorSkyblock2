package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CmdSetPermission implements ICommand {

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
        return "island setpermission <island-role/player-name> <island-permission> <true/false>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_SET_PERMISSION.getMessage();
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPermission.SET_PERMISSION)){
            Locale.NO_PERMISSION_SET_PERMISSION.send(superiorPlayer, island.getRequiredRole(IslandPermission.SET_PERMISSION));
            return;
        }

        Object permssionHolder;
        String permissionHolderName;
        IslandRole islandRole;
        IslandPermission islandPermission;
        boolean value;

        try{
            islandRole = IslandRole.valueOf(args[1].toUpperCase());
            permssionHolder = islandRole;
            permissionHolderName = islandRole.name();
        }catch(IllegalArgumentException ex){
            SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

            if(targetPlayer == null){
                Locale.INVALID_PLAYER.send(superiorPlayer, args[1]);
                return;
            }

            islandRole = island.isMember(targetPlayer) ? targetPlayer.getIslandRole() : IslandRole.GUEST;
            permssionHolder = targetPlayer;
            permissionHolderName = targetPlayer.getName();
        }

        if(!islandRole.isLessThan(superiorPlayer.getIslandRole())){
            Locale.CHANGE_PERMISSION_FOR_HIGHER_ROLE.send(superiorPlayer);
            return;
        }

        try{
            islandPermission = IslandPermission.valueOf(args[2].toUpperCase());
        }catch(IllegalArgumentException ex){
            Locale.INVALID_ISLAND_PERMISSION.send(superiorPlayer, args[2], IslandPermission.getValuesString());
            return;
        }

        if(!superiorPlayer.hasPermission(islandPermission)){
            Locale.LACK_CHANGE_PERMISSION.send(superiorPlayer);
            return;
        }

        try{
            value = Boolean.parseBoolean(args[3]);
        }catch(IllegalArgumentException ex){
            Locale.INVALID_BOOLEAN.send(superiorPlayer);
            return;
        }

        if(permssionHolder instanceof IslandRole)
            island.setPermission((IslandRole) permssionHolder, islandPermission, value);
        else
            island.setPermission((SuperiorPlayer) permssionHolder, islandPermission, value);

        Locale.UPDATED_PERMISSION.send(superiorPlayer, permissionHolderName);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island != null && superiorPlayer.hasPermission(IslandPermission.SET_PERMISSION)){
            List<String> list = new ArrayList<>();

            if (args.length == 2) {
                for(IslandRole islandRole : IslandRole.values()) {
                    if(islandRole.name().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(islandRole.name().toLowerCase());
                }

                for(Player player : Bukkit.getOnlinePlayers()){
                    if(player.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(player.getName().toLowerCase());
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
