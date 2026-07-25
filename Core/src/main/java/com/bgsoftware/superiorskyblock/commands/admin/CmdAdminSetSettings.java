package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetSettings implements IAdminIslandCommand {

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
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_SETTINGS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_VALUE.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_SETTINGS.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        IslandFlag islandFlag = CommandArguments.getIslandFlag(sender, args[3]);

        if (islandFlag == null)
            return;

        boolean value = args[4].equalsIgnoreCase("true");

        int islandsChangedCount = 0;

        for (Island island : islands) {
            if (island.hasSettingsEnabled(islandFlag) == value) {
                ++islandsChangedCount;
                continue;
            }

            if (value) {
                if (PluginEventsFactory.callIslandEnableFlagEvent(island, sender, islandFlag)) {
                    ++islandsChangedCount;
                    island.enableSettings(islandFlag);
                }
            } else if (PluginEventsFactory.callIslandDisableFlagEvent(island, sender, islandFlag)) {
                ++islandsChangedCount;
                island.disableSettings(islandFlag);
            }
        }

        if (islandsChangedCount <= 0)
            return;

        if (islands.size() != 1)
            Message.SETTINGS_UPDATED_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(islandFlag.getName()));
        else if (targetPlayer == null)
            Message.SETTINGS_UPDATED_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(islandFlag.getName()), islands.get(0).getName());
        else
            Message.SETTINGS_UPDATED.send(sender, Formatters.CAPITALIZED_FORMATTER.format(islandFlag.getName()), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getIslandFlags(args[3]) :
                args.length == 5 ? CommandTabCompletes.getCustomComplete(args[4], "true", "false") :
                        Collections.emptyList();
    }

}
