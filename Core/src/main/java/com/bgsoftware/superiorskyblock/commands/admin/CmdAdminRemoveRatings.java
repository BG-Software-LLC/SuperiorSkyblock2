package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CmdAdminRemoveRatings implements IAdminIslandCommand {
    @Override
    public List<String> getAliases() {
        return Arrays.asList("removeratings", "rratings", "rr");
    }

    @Override
    public String getPermission() {
        return "superior.admin.removeratings";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin removeratings <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_REMOVE_RATINGS.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
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
        boolean removingAllRatings = targetPlayer == null;
        Collection<Island> iterIslands = removingAllRatings ? islands : plugin.getGrid().getIslands();

        int islandsChangedCount = 0;

        for (Island island : iterIslands) {
            if (removingAllRatings) {
                if (PluginEventsFactory.callIslandClearRatingsEvent(island, sender)) {
                    ++islandsChangedCount;
                    island.removeRatings();
                }
            } else if (PluginEventsFactory.callIslandRemoveRatingEvent(island, sender, targetPlayer)) {
                ++islandsChangedCount;
                island.removeRating(targetPlayer);
            }
        }

        if (islandsChangedCount <= 0)
            return;

        if (!removingAllRatings)
            Message.RATE_REMOVE_ALL.send(sender, targetPlayer.getName());
        else if (islands.size() == 1)
            Message.RATE_REMOVE_ALL.send(sender, islands.get(0).getName());
        else
            Message.RATE_REMOVE_ALL_ISLANDS.send(sender);
    }

}
