package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdAdminAddWarpsLimit implements IAdminIslandCommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addwarpslimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addwarpslimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addwarpslimit <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_WARPS_LIMIT.getMessage(locale);
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

        if (!arguments.getValue())
            return;

        int limit = arguments.getKey();

        if (limit < 0) {
            Message.INVALID_AMOUNT.send(sender);
            return;
        }

        Executor.data(() -> islands.forEach(island -> island.setWarpsLimit(island.getWarpsLimit() + limit)));

        if (islands.size() > 1)
            Message.CHANGED_WARPS_LIMIT_ALL.send(sender);
        else if (targetPlayer == null)
            Message.CHANGED_WARPS_LIMIT_NAME.send(sender, islands.get(0).getName());
        else
            Message.CHANGED_WARPS_LIMIT.send(sender, targetPlayer.getName());
    }

}
