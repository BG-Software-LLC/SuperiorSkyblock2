package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminSetRoleLimit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setrolelimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setrolelimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setrolelimit <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_ISLAND_ROLE.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_ROLE_LIMIT.getMessage(locale);
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
        PlayerRole playerRole = CommandArguments.getPlayerRole(sender, args[3]);

        if(playerRole == null)
            return;

        if(!IslandUtils.isValidRoleForLimit(playerRole)){
            Locale.INVALID_ROLE.send(sender, args[3], SPlayerRole.getValuesString());
            return;
        }

        Pair<Integer, Boolean> arguments = CommandArguments.getLimit(sender, args[4]);

        if(!arguments.getValue())
            return;

        int limit = arguments.getKey();

        Executor.data(() -> islands.forEach(island -> island.setRoleLimit(playerRole, limit)));

        if(islands.size() > 1)
            Locale.CHANGED_ROLE_LIMIT_ALL.send(sender, playerRole);
        else if(targetPlayer == null)
            Locale.CHANGED_ROLE_LIMIT_NAME.send(sender, playerRole, islands.get(0).getName());
        else
            Locale.CHANGED_ROLE_LIMIT.send(sender, playerRole, targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getPlayerRoles(plugin, args[3],
                IslandUtils::isValidRoleForLimit) : new ArrayList<>();
    }

}
