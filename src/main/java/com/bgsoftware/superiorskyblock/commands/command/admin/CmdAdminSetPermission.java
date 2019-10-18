package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdAdminSetPermission implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setpermission", "setperm");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setpermission";
    }

    @Override
    public String getUsage() {
        return "island admin setpermission <permission> <role>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_PERMISSION.getMessage();
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
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        IslandPermission islandPermission;

        try{
            islandPermission = IslandPermission.valueOf(args[2].toUpperCase());
        }catch(IllegalArgumentException ex){
            Locale.INVALID_ISLAND_PERMISSION.send(sender, args[2], StringUtils.getPermissionsString());
            return;
        }

        PlayerRole playerRole;

        try{
            playerRole = SPlayerRole.of(args[3]);
        }catch(IllegalArgumentException ex){
            Locale.INVALID_ROLE.send(sender, args[2], SPlayerRole.getValuesString());
            return;
        }

        plugin.getGrid().getIslands().forEach(island -> island.setPermission(playerRole, islandPermission, true));

        Locale.PERMISSION_CHANGED.send(sender);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            list.addAll(Arrays.stream(IslandPermission.values())
                    .map(islandPermission -> islandPermission.toString().toLowerCase())
                    .filter(islandPermissionName -> islandPermissionName.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList())
            );
        }
        else if(args.length == 4){
            list.addAll(plugin.getPlayers().getRoles().stream()
                    .map(playerRole -> playerRole.toString().toLowerCase())
                    .filter(playerRoleName -> playerRoleName.startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList())
            );
        }

        return list;
    }
}
