package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandArgument;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdRate implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("rate");
    }

    @Override
    public String getPermission() {
        return "superior.island.rate";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "rate [" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_RATE.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        IslandArgument arguments = args.length == 1 ? CommandArguments.getIslandWhereStanding(plugin, sender) :
                CommandArguments.getIsland(plugin, sender, args[1]);

        Island island = arguments.getIsland();

        if (island == null)
            return;

        if (island.isSpawn()) {
            Message.INVALID_ISLAND_LOCATION.send(sender);
            return;
        }

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        RateResult rateResult = canRateIsland(plugin, superiorPlayer, island);

        switch (rateResult) {
            case RATE_OWN_ISLAND:
                Message.RATE_OWN_ISLAND.send(superiorPlayer);
                return;
            case ALREADY_RATED:
                Message.RATE_ALREADY_GIVEN.send(superiorPlayer);
                return;
        }

        plugin.getMenus().openIslandRate(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island island = superiorPlayer.getIsland();
        return args.length == 2 ? CommandTabCompletes.getOnlinePlayersWithIslands(plugin, args[1],
                plugin.getSettings().isTabCompleteHideVanished(), (onlinePlayer, onlineIsland) ->
                        canRateIsland(plugin, superiorPlayer, island, onlineIsland) == RateResult.SUCCESS) :
                Collections.emptyList();
    }

    private static RateResult canRateIsland(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island) {
        return canRateIsland(plugin, superiorPlayer, superiorPlayer.getIsland(), island);
    }

    private static RateResult canRateIsland(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer,
                                            Island playerIsland, Island island) {
        if (!plugin.getSettings().isRateOwnIsland() && island.equals(playerIsland)) {
            return RateResult.RATE_OWN_ISLAND;
        }

        if (!plugin.getSettings().isChangeIslandRating() && island.getRating(superiorPlayer) != Rating.UNKNOWN) {
            return RateResult.ALREADY_RATED;
        }

        return RateResult.SUCCESS;
    }

    private enum RateResult {

        SUCCESS,
        RATE_OWN_ISLAND,
        ALREADY_RATED

    }

}
