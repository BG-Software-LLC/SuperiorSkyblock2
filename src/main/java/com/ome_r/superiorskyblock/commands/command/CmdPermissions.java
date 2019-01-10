package com.ome_r.superiorskyblock.commands.command;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.commands.ICommand;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.island.IslandPermission;
import com.ome_r.superiorskyblock.island.IslandRole;
import com.ome_r.superiorskyblock.island.PermissionNode;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdPermissions implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("permissions", "perms");
    }

    @Override
    public String getPermission() {
        return "superior.island.permissions";
    }

    @Override
    public String getUsage() {
        return "island permissions <island-role>";
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

        if(!wrappedPlayer.hasPermission(IslandPermission.CHECK_PERMISSION)){
            Locale.NO_PERMISSION_CHECK_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.CHECK_PERMISSION));
            return;
        }

        IslandRole islandRole;

        try{
            islandRole = IslandRole.valueOf(args[1].toUpperCase());
        }catch(IllegalArgumentException ex){
            Locale.INVALID_ROLE.send(wrappedPlayer, args[1], IslandRole.getValuesString());
            return;
        }

        PermissionNode permissionNode = island.getPermisisonNode(islandRole);

        Locale.PERMISSION_CHECK.send(wrappedPlayer, islandRole, permissionNode.getColoredPermissions());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(args.length == 2 && island != null && wrappedPlayer.hasPermission(IslandPermission.CHECK_PERMISSION)){
            List<String> list = new ArrayList<>();

            for(IslandRole islandRole : IslandRole.values()) {
                if(islandRole.name().toLowerCase().startsWith(args[1].toLowerCase()))
                    list.add(islandRole.name().toLowerCase());
            }

            return list;
        }

        return new ArrayList<>();
    }
}
