package com.bgsoftware.superiorskyblock.module.visit.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandArgument;
import com.bgsoftware.superiorskyblock.core.IslandWorlds;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuDimensionSelection;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.visit.utils.VisitUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdVisit implements ISuperiorCommand {

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
        StringBuilder usage = new StringBuilder("visit ");

        usage.append(BuiltinModules.VISIT.getConfiguration().isMenusVisitIslandsEnabled() ? "[" : "<");

        usage.append(Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale)).append("/")
                .append(Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale));

        usage.append(BuiltinModules.VISIT.getConfiguration().isMenusVisitIslandsEnabled() ? "]" : ">");

        if (!BuiltinModules.VISIT.getConfiguration().isOnlyDefaultDimension()) {
            usage.append(" [").append(Message.COMMAND_ARGUMENT_DIMENSION.getMessage(locale)).append("]");
        }

        return usage.toString();
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_VISIT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return BuiltinModules.VISIT.getConfiguration().isMenusVisitIslandsEnabled() ? 1 : 2;
    }

    @Override
    public int getMaxArgs() {
        return BuiltinModules.VISIT.getConfiguration().isOnlyDefaultDimension() ? 2 : 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Dimension defaultDimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();
        boolean onlyDefaultDimension = BuiltinModules.VISIT.getConfiguration().isOnlyDefaultDimension();
        boolean visitIslandsMenu = BuiltinModules.VISIT.getConfiguration().isMenusVisitIslandsEnabled();

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        switch (args.length) {
            case 1: { // The visit islands menu must be enabled to allow for this number of arguments.
                if (onlyDefaultDimension) {
                    openVisitIslands(plugin, superiorPlayer, defaultDimension);
                } else {
                    plugin.getMenus().openDimensionSelection(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()),
                            dimension -> openVisitIslands(plugin, superiorPlayer, dimension));
                }

                return;
            }
            case 2: {
                if (visitIslandsMenu && !onlyDefaultDimension) {
                    Dimension dimension = getDimensionSafe(args[1]);

                    if (dimension != null && plugin.getProviders().getWorldsProvider().isDimensionEnabled(dimension)) {
                        openVisitIslands(plugin, superiorPlayer, dimension);
                        return;
                    }
                }

                IslandArgument islandArgument = CommandArguments.getIsland(plugin, sender, args[1]);
                Island island = islandArgument.getIsland();

                if (island == null) {
                    return;
                }

                SuperiorPlayer targetPlayer = islandArgument.getSuperiorPlayer();
                if (onlyDefaultDimension) {
                    teleportPlayerInternal(superiorPlayer, island, targetPlayer, defaultDimension);
                } else {
                    plugin.getMenus().openDimensionSelection(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()),
                            dimension -> teleportPlayerInternal(superiorPlayer, island, targetPlayer, dimension));
                }

                return;
            }
            case 3: { // The only default dimension must be disabled to allow for this number of arguments.
                IslandArgument islandArgument = CommandArguments.getIsland(plugin, sender, args[1]);
                Island island = islandArgument.getIsland();

                if (island == null) {
                    return;
                }

                Dimension dimension = CommandArguments.getEnabledDimension(plugin, sender, args[2]);

                if (dimension == null) {
                    return;
                }

                SuperiorPlayer targetPlayer = islandArgument.getSuperiorPlayer();
                teleportPlayerInternal(superiorPlayer, island, targetPlayer, dimension);
            }
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        if (args.length == 2) {
            return CommandTabCompletes.getOnlinePlayersAndIslands(plugin, args[1],
                    plugin.getSettings().isTabCompleteHideVanished(), (onlinePlayer, onlineIsland) ->
                    onlineIsland != null && ((!BuiltinModules.VISIT.getConfiguration().isSignsRequiredForVisit() ||
                            !onlineIsland.getVisitorHomesPositions().isEmpty()) ||
                            superiorPlayer.hasBypassModeEnabled()) && (!onlineIsland.isLocked() ||
                            onlineIsland.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)));
        } else if (args.length == 3 && !BuiltinModules.VISIT.getConfiguration().isOnlyDefaultDimension()) {
            if (getDimensionSafe(args[1]) == null) {
                Island island = CommandTabCompletes.getIsland(plugin, args[1]);

                if (island != null) {
                    if (BuiltinModules.VISIT.getConfiguration().isSignsRequiredForVisit()) {
                        List<String> dimensions = new ArrayList<>();

                        for (Dimension dimension : island.getVisitorHomesPositions().keySet() ) {
                            dimensions.add(dimension.getName().toLowerCase(Locale.ENGLISH));
                        }

                        return CommandTabCompletes.getCustomComplete(args[2], dimensions);
                    } else {
                        return CommandTabCompletes.getDimensions(plugin, args[2]);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private static void teleportPlayerInternal(SuperiorPlayer superiorPlayer, Island island,
                                               SuperiorPlayer targetPlayer, Dimension dimension) {
        if (!PluginEventsFactory.callIslandVisitorHomeTeleportEvent(island, superiorPlayer, dimension)) {
            return;
        }

        IslandWorlds.accessIslandWorldAsync(island, dimension, true, islandWorldResult ->
                islandWorldResult.ifLeft(unused -> VisitUtils.teleportPlayerInternal(superiorPlayer, island, targetPlayer, dimension)));

        MenuView<?, ?> menuView = superiorPlayer.getOpenedView();

        if (menuView != null && menuView.getMenu() instanceof MenuDimensionSelection) {
            menuView.setPreviousMove(false);
            BukkitExecutor.sync(menuView::closeView, 1L);
        }
    }

    private static void openVisitIslands(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Dimension dimension) {
        plugin.getMenus().openVisitIslands(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()),
                SortingTypes.getVisitIslandsSortingType(), dimension);
    }

    @Nullable
    private static Dimension getDimensionSafe(String name) {
        try {
            return Dimension.getByName(name);
        } catch (Exception ignored) {
            return null;
        }
    }

}
