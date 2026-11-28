package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BulkChange;
import com.bgsoftware.superiorskyblock.commands.ChangeResult;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminAddCoopLimit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addcooplimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addcooplimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addcooplimit <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_COOP_LIMIT.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        NumberArgument<Integer> arguments = CommandArguments.getAdditionalLimit(sender, args[3]);

        if (!arguments.isSucceed()) {
            return;
        }

        int limit = arguments.getNumber();

        BulkChange<Island> change = BulkChange.ofIslands(islands, island -> {
            int currentLimit = island.getCoopLimit();
            if (currentLimit <= IslandUpgradeConstants.NO_LIMIT_VALUE) {
                return ChangeResult.UNLIMITED;
            }

            int newLimit = currentLimit + limit;

            if (newLimit < 0) {
                return ChangeResult.BELOW_MINIMUM;
            }

            return ChangeResult.ifAllowed(PluginEventsFactory.callIslandChangeCoopLimitEvent(island, sender, newLimit),
                    eventArgs -> eventArgs.island.setCoopLimit(eventArgs.coopLimit));
        });

        change.sendSummary(sender, args);
    }

}
