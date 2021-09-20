package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdAdminSetPermission implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setpermission", "setperm");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setpermission";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setpermission <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_PERMISSION.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_ISLAND_ROLE.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_PERMISSION.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        IslandPrivilege islandPrivilege = CommandArguments.getIslandPrivilege(sender, args[3]);

        if(islandPrivilege == null)
            return;

        PlayerRole playerRole = CommandArguments.getPlayerRole(sender, args[4]);

        if(playerRole == null)
            return;

        Executor.data(() -> islands.forEach(island -> island.setPermission(playerRole, islandPrivilege, true)));

        if(islands.size() > 1)
            Locale.PERMISSION_CHANGED_ALL.send(sender, StringUtils.format(islandPrivilege.getName()));
        else if(targetPlayer == null)
            Locale.PERMISSION_CHANGED_NAME.send(sender, StringUtils.format(islandPrivilege.getName()), islands.get(0).getName());
        else
            Locale.PERMISSION_CHANGED.send(sender, StringUtils.format(islandPrivilege.getName()), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getIslandPrivileges(args[3]) :
                args.length == 5 ? CommandTabCompletes.getPlayerRoles(plugin, args[4]) : new ArrayList<>();
    }

}
