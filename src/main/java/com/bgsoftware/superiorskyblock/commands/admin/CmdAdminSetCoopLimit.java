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

public final class CmdAdminSetCoopLimit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setcooplimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setcooplimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setcooplimit <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_COOP_LIMIT.getMessage(locale);
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

        Executor.data(() -> islands.forEach(island -> island.setCoopLimit(limit)));

        if(islands.size() > 1)
            Locale.CHANGED_COOP_LIMIT_ALL.send(sender);
        else if(targetPlayer == null)
            Locale.CHANGED_COOP_LIMIT_NAME.send(sender, islands.get(0).getName());
        else
            Locale.CHANGED_COOP_LIMIT.send(sender, targetPlayer.getName());
    }

}
