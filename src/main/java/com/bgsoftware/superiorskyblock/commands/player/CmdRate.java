package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdRate implements ISuperiorCommand {

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
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_RATE.getMessage(locale);
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
        Pair<Island, SuperiorPlayer> arguments = args.length == 1 ? CommandArguments.getIslandWhereStanding(plugin, sender) :
                CommandArguments.getIsland(plugin, sender, args[1]);

        Island island = arguments.getKey();

        if(island == null)
            return;

        if(island.isSpawn()){
            Locale.INVALID_ISLAND_LOCATION.send(sender);
            return;
        }

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        if(!plugin.getSettings().isRateOwnIsland() && island.equals(superiorPlayer.getIsland())){
            Locale.RATE_OWN_ISLAND.send(superiorPlayer);
            return;
        }

        plugin.getMenus().openIslandRate(superiorPlayer, null, island);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island island = superiorPlayer.getIsland();
        return args.length == 2 ? CommandTabCompletes.getOnlinePlayersWithIslands(plugin, args[1],
                plugin.getSettings().isTabCompleteHideVanished(),
                (onlinePlayer, onlineIsland) -> onlineIsland != null &&
                        (plugin.getSettings().isRateOwnIsland() || !onlineIsland.equals(island))) : new ArrayList<>();
    }

}
