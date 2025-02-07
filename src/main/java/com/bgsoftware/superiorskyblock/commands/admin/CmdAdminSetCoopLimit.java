package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetCoopLimit implements IAdminIslandCommand {

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
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_COOP_LIMIT.getMessage(locale);
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
        NumberArgument<Integer> arguments = CommandArguments.getLimit(sender, args[3]);

        if (!arguments.isSucceed())
            return;

        int limit = arguments.getNumber();

        int islandsChangedCount = 0;

        for (Island island : islands) {
            PluginEvent<PluginEventArgs.IslandChangeCoopLimit> event = PluginEventsFactory.callIslandChangeCoopLimitEvent(
                    island, sender, island.getCoopLimit() + limit);
            if (!event.isCancelled()) {
                island.setCoopLimit(event.getArgs().coopLimit);
                ++islandsChangedCount;
            }
        }

        if (islandsChangedCount <= 0)
            return;

        if (islandsChangedCount > 1)
            Message.CHANGED_COOP_LIMIT_ALL.send(sender);
        else if (targetPlayer == null)
            Message.CHANGED_COOP_LIMIT_NAME.send(sender, islands.get(0).getName());
        else
            Message.CHANGED_COOP_LIMIT.send(sender, targetPlayer.getName());
    }

}
