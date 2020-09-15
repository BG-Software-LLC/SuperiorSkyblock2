package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.menu.MenuPermissions;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdPermissions implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("permissions", "perms", "setpermission", "setperm");
    }

    @Override
    public String getPermission() {
        return "superior.island.permissions";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "permissions [" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "] [reset]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_PERMISSIONS.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 3;
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

        if(!superiorPlayer.hasPermission(IslandPrivileges.SET_PERMISSION)){
            Locale.NO_PERMISSION_CHECK_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.SET_PERMISSION));
            return;
        }

        Object permissionHolder = SPlayerRole.guestRole();

        boolean setToDefault = (args.length == 2 ? args[1] : args.length == 3 ? args[2] : "").equalsIgnoreCase("reset");

        if((!setToDefault && args.length == 2) || args.length == 3){
            SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

            if(targetPlayer == null){
                Locale.INVALID_PLAYER.send(superiorPlayer, args[1]);
                return;
            }

            if(island.isMember(targetPlayer) && !superiorPlayer.getPlayerRole().isHigherThan(targetPlayer.getPlayerRole())){
                Locale.CHANGE_PERMISSION_FOR_HIGHER_ROLE.send(superiorPlayer);
                return;
            }

            permissionHolder = targetPlayer;
        }

        if(!setToDefault){
            MenuPermissions.openInventory(superiorPlayer, null, island, permissionHolder);
        }

        else{
            if(permissionHolder instanceof PlayerRole) {
                island.resetPermissions();
                Locale.PERMISSIONS_RESET_ROLES.send(sender);
            }
            else {
                island.resetPermissions((SuperiorPlayer) permissionHolder);
                Locale.PERMISSIONS_RESET_PLAYER.send(sender, ((SuperiorPlayer) permissionHolder).getName());
            }
        }

    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island != null && superiorPlayer.hasPermission(IslandPrivileges.SET_PERMISSION)){
            List<String> list = new ArrayList<>();

            if(args.length == 2) {
                if("reset".contains(args[1].toLowerCase()))
                    list.add("reset");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().contains(args[1].toLowerCase()))
                        list.add(player.getName().toLowerCase());
                }
            }
            if(args.length == 3){
                if("reset".contains(args[2].toLowerCase()))
                    list.add("reset");
            }

            return list;
        }

        return new ArrayList<>();
    }
}
