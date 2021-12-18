package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public final class CmdAdminRemoveRatings implements IAdminIslandCommand {
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        Executor.data(() -> {
            if (targetPlayer != null) {
                plugin.getGrid().getIslands().forEach(island -> island.setRating(targetPlayer, Rating.UNKNOWN));
            } else {
                islands.forEach(Island::removeRatings);
            }
        });

        if (targetPlayer != null)
            Message.RATE_REMOVE_ALL.send(sender, targetPlayer.getName());
        else if (islands.size() == 1)
            Message.RATE_REMOVE_ALL.send(sender, islands.get(0).getName());
        else
            Message.RATE_REMOVE_ALL_ISLANDS.send(sender);
    }

}
