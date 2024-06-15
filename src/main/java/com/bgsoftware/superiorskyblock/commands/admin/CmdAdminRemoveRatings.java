package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BaseCommand;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CmdAdminRemoveRatings extends BaseCommand implements IAdminIslandCommand {
    @Override
    protected List<String> aliases() {
        return Lists.newLinkedList("removeratings", "rratings", "rr");
    }

    @Override
    protected String permission() {
        return "superior.admin.removeratings";
    }

    @Override
    protected String usage(java.util.Locale locale) {
        return "admin removeratings <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + ">";
    }

    @Override
    protected String description(java.util.Locale locale) {
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

        boolean anyIslandChanged = false;

        for (Island island : iterIslands) {
            if (removingAllRatings) {
                if (plugin.getEventsBus().callIslandClearRatingsEvent(sender, island)) {
                    anyIslandChanged = true;
                    island.removeRatings();
                }
            } else if (plugin.getEventsBus().callIslandRemoveRatingEvent(sender, targetPlayer, island)) {
                anyIslandChanged = true;
                island.removeRating(targetPlayer);
            }
        }

        if (!anyIslandChanged)
            return;

        if (!removingAllRatings)
            Message.RATE_REMOVE_ALL.send(sender, targetPlayer.getName());
        else if (islands.size() == 1)
            Message.RATE_REMOVE_ALL.send(sender, islands.get(0).getName());
        else
            Message.RATE_REMOVE_ALL_ISLANDS.send(sender);
    }

}
