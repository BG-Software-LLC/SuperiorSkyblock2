package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminSetRate implements IAdminIslandCommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setrate");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setrate";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setrate <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_RATING.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_RATE.getMessage(locale);
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
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, sender, args[3]);

        if(targetPlayer == null)
            return;

        Rating rating = CommandArguments.getRating(sender, args[4]);

        if(rating == null)
            return;

        island.setRating(targetPlayer, rating);

        Locale.RATE_CHANGE_OTHER.send(sender, targetPlayer.getName(), StringUtils.format(rating.name()));
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getRatedPlayers(plugin, island, args[3]) :
                args.length == 5 ? CommandTabCompletes.getRatings(args[4]) : new ArrayList<>();
    }

}
