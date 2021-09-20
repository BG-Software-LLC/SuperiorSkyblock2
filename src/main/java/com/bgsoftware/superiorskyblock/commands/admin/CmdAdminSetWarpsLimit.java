package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdAdminSetWarpsLimit implements IAdminIslandCommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setwarpslimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setwarpslimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setwarpslimit <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_WARPS_LIMIT.getMessage(locale);
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
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        Pair<Integer, Boolean> arguments = CommandArguments.getLimit(sender, args[3]);

        if(!arguments.getValue())
            return;

        int limit = arguments.getKey();

        if (limit < 0) {
            Locale.INVALID_AMOUNT.send(sender);
            return;
        }

        Executor.data(() -> islands.forEach(island -> island.setWarpsLimit(limit)));

        if(islands.size() > 1)
            Locale.CHANGED_WARPS_LIMIT_ALL.send(sender);
        else if(targetPlayer == null)
            Locale.CHANGED_WARPS_LIMIT_NAME.send(sender, islands.get(0).getName());
        else
            Locale.CHANGED_WARPS_LIMIT.send(sender, targetPlayer.getName());
    }

}
