package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.menu.IslandPermissionsMenu;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdPermissions implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("permissions", "perms", "setpermission", "setperm");
    }

    @Override
    public String getPermission() {
        return "superior.island.permissions";
    }

    @Override
    public String getUsage() {
        return "permissions [" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage() + "]";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_PERMISSIONS.getMessage();
    }

    @Override
    public int getMinArgs() {
        return 1;
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

        if(!superiorPlayer.hasPermission(IslandPermission.SET_PERMISSION)){
            Locale.NO_PERMISSION_CHECK_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPermission.SET_PERMISSION));
            return;
        }

        Object permissionHolder = SPlayerRole.guestRole();

        if(args.length == 2){
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

        IslandPermissionsMenu.openInventory(superiorPlayer, null, island, permissionHolder);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPermission.SET_PERMISSION)){
            List<String> list = new ArrayList<>();

            for(Player player : Bukkit.getOnlinePlayers()){
                if(player.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                    list.add(player.getName().toLowerCase());
            }

            return list;
        }

        return new ArrayList<>();
    }
}
