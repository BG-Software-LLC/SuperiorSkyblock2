package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdVisit implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("visit");
    }

    @Override
    public String getPermission() {
        return "superior.island.visit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "visit <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_VISIT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
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
        Island targetIsland = CommandArguments.getIsland(plugin, sender, args[1]).getKey();

        if(targetIsland == null)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        Location visitLocation = targetIsland.getVisitorsLocation();

        if(visitLocation == null){
            Locale.INVALID_VISIT_LOCATION.send(sender);

            if(!superiorPlayer.hasBypassModeEnabled())
                return;

            visitLocation = targetIsland.getTeleportLocation(plugin.getSettings().getWorlds().getDefaultWorld());
            Locale.INVALID_VISIT_LOCATION_BYPASS.send(sender);
        }

        if(targetIsland.isLocked() && !targetIsland.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)){
            Locale.NO_CLOSE_BYPASS.send(sender);
            return;
        }

        superiorPlayer.teleport(visitLocation);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        return args.length == 2 ? CommandTabCompletes.getOnlinePlayersWithIslands(plugin, args[1],
                plugin.getSettings().isTabCompleteHideVanished(),
                (onlinePlayer, onlineIsland) -> onlineIsland != null && (onlineIsland.getVisitorsLocation() != null ||
                        superiorPlayer.hasBypassModeEnabled()) && (!onlineIsland.isLocked() ||
                        onlineIsland.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS))) : new ArrayList<>();
    }

}
