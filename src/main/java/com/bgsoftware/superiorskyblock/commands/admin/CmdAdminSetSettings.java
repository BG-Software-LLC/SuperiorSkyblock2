package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminSetSettings implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setsettings");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setsettings";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setsettings <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_SETTINGS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_VALUE.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_SETTINGS.getMessage(locale);
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
        IslandFlag islandFlag = CommandArguments.getIslandFlag(sender, args[3]);

        if(islandFlag == null)
            return;

        boolean value = args[4].equalsIgnoreCase("true");

        Executor.data(() -> islands.forEach(island -> {
            if(value)
                island.enableSettings(islandFlag);
            else
                island.disableSettings(islandFlag);
        }));

        if(islands.size() != 1)
            Locale.SETTINGS_UPDATED_ALL.send(sender, StringUtils.format(islandFlag.getName()));
        else if(targetPlayer == null)
            Locale.SETTINGS_UPDATED_NAME.send(sender, StringUtils.format(islandFlag.getName()), islands.get(0).getName());
        else
            Locale.SETTINGS_UPDATED.send(sender, StringUtils.format(islandFlag.getName()), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getIslandFlags(args[3]) :
                args.length == 5 ?  CommandTabCompletes.getCustomComplete(args[4], "true", "false") : new ArrayList<>();
    }

}
