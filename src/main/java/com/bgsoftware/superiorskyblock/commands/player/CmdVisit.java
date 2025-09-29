package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.LazyWorldsProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.IslandWorlds;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import com.bgsoftware.superiorskyblock.world.WorldBlocks;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdVisit implements ISuperiorCommand {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

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
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_VISIT.getMessage(locale);
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
        Island targetIsland = CommandArguments.getIsland(plugin, sender, args[1]).getIsland();

        if (targetIsland == null)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Dimension dimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();

        if (!PluginEventsFactory.callIslandVisitorHomeTeleportEvent(targetIsland, superiorPlayer, dimension))
            return;

        IslandWorlds.accessIslandWorldAsync(targetIsland, dimension, true, islandWorldResult ->
                islandWorldResult.ifLeft(world -> teleportPlayerInternal(targetIsland, superiorPlayer)));
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        return args.length == 2 ? CommandTabCompletes.getOnlinePlayersWithIslands(plugin, args[1],
                plugin.getSettings().isTabCompleteHideVanished(), (onlinePlayer, onlineIsland) ->
                        ((!plugin.getSettings().getVisitorsSign().isRequiredForVisit() ||
                                onlineIsland.getVisitorsLocation((Dimension) null /* unused */) != null) ||
                                superiorPlayer.hasBypassModeEnabled()) && (!onlineIsland.isLocked() ||
                        onlineIsland.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS))) : Collections.emptyList();
    }

    private static void teleportPlayerInternal(Island targetIsland, SuperiorPlayer superiorPlayer) {
        Location visitLocation;
        boolean isVisitorSign;

        if (plugin.getSettings().getVisitorsSign().isRequiredForVisit()) {
            isVisitorSign = true;
            visitLocation = targetIsland.getVisitorsLocation((Dimension) null /* unused */);
        } else {
            isVisitorSign = false;
            visitLocation = targetIsland.getIslandHome(plugin.getSettings().getWorlds().getDefaultWorldDimension());
        }

        if (visitLocation == null) {
            Message.INVALID_VISIT_LOCATION.send(superiorPlayer);

            if (!superiorPlayer.hasBypassModeEnabled())
                return;

            visitLocation = targetIsland.getIslandHome(plugin.getSettings().getWorlds().getDefaultWorldDimension());
            Message.INVALID_VISIT_LOCATION_BYPASS.send(superiorPlayer);
        }

        if (targetIsland.isLocked() && !targetIsland.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)) {
            Message.NO_CLOSE_BYPASS.send(superiorPlayer);
            return;
        }

        Location finalVisitLocation = visitLocation;

        EntityTeleports.warmupTeleport(superiorPlayer, plugin.getSettings().getVisitWarmup(), afterWarmup ->
                teleportPlayerNoWarmup(superiorPlayer, targetIsland, finalVisitLocation,
                        isVisitorSign, afterWarmup /*checkIslandLock*/, true));
    }

    private static void teleportPlayerNoWarmup(SuperiorPlayer superiorPlayer, Island island, Location visitLocation,
                                               boolean isVisitorSign, boolean checkIslandLock, boolean shouldRetryOnNullWorld) {
        if (visitLocation.getWorld() == null) {
            if (shouldRetryOnNullWorld && visitLocation instanceof LazyWorldLocation &&
                    plugin.getProviders().getWorldsProvider() instanceof LazyWorldsProvider) {
                LazyWorldsProvider worldsProvider = (LazyWorldsProvider) plugin.getProviders().getWorldsProvider();
                WorldInfo worldInfo = worldsProvider.getIslandsWorldInfo(island, ((LazyWorldLocation) visitLocation).getWorldName());
                worldsProvider.prepareWorld(island, worldInfo.getDimension(),
                        () -> teleportPlayerNoWarmup(superiorPlayer, island, visitLocation, isVisitorSign, checkIslandLock, false));
                return;
            }
        }

        superiorPlayer.setTeleportTask(null);

        if (checkIslandLock && island.isLocked() && !island.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)) {
            Message.NO_CLOSE_BYPASS.send(superiorPlayer);
            return;
        }

        if (isVisitorSign && !WorldBlocks.isSafeBlock(visitLocation.getBlock())) {
            Message.INVALID_VISIT_LOCATION.send(superiorPlayer);

            if (!superiorPlayer.hasBypassModeEnabled()) {
                if (PluginEventsFactory.callIslandRemoveVisitorHomeEvent(island, superiorPlayer))
                    island.setVisitorsLocation(null);
                return;
            }

            Message.INVALID_VISIT_LOCATION_BYPASS.send(superiorPlayer);
        }

        superiorPlayer.teleport(visitLocation);
    }

}
