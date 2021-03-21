package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdAdminCmdAll implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("cmdall");
    }

    @Override
    public String getPermission() {
        return "superior.admin.cmdall";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin cmdall <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <online-filter[true/false]> <" +
                Locale.COMMAND_ARGUMENT_COMMAND.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_CMD_ALL.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return Integer.MAX_VALUE;
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
        String command = CommandArguments.buildLongString(args, 4, false);
        boolean onlyOnline = Boolean.parseBoolean(args[3]);

        islands.forEach(island -> island.executeCommand(command, onlyOnline));

        if(targetPlayer == null)
            Locale.GLOBAL_COMMAND_EXECUTED_NAME.send(sender, islands.size() == 1 ? islands.get(0).getName() : "all");
        else
            Locale.GLOBAL_COMMAND_EXECUTED.send(sender, targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getCustomComplete(args[3], "true", "false") : null;
    }

}
