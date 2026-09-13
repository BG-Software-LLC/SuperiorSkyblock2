package com.bgsoftware.superiorskyblock.island.top;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.module.visit.VisitModule;
import com.bgsoftware.superiorskyblock.module.warps.WarpsModule;

import java.util.Comparator;
import java.util.function.Function;

public class SortingTypes {

    public static final SortingType BY_BANK = register("BANK", island -> island.getIslandBank().getBalance(), SortingComparators.BANK_COMPARATOR);
    public static final SortingType BY_LEVEL = register("LEVEL", Island::getIslandLevel, SortingComparators.LEVEL_COMPARATOR);
    public static final SortingType BY_PLAYERS = register("PLAYERS", island -> island.getAllPlayersInside().size(), SortingComparators.PLAYERS_COMPARATOR);
    public static final SortingType BY_RATING = register("RATING", Island::getTotalRating, SortingComparators.RATING_COMPARATOR);
    public static final SortingType BY_WORTH = register("WORTH", Island::getWorth, SortingComparators.WORTH_COMPARATOR);

    private static volatile SortingType GLOBAL_WARPS_SORTING;
    private static volatile SortingType TOP_ISLANDS_SORTING;
    private static volatile SortingType VISIT_ISLANDS_SORTING;

    private SortingTypes() {
    }

    public static void registerSortingTypes(SuperiorSkyblockPlugin plugin) {
        // We actually register the settings update listener in here, as otherwise it causes errors
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/2752
        registerListeners(plugin.getPluginEventsDispatcher());
    }

    private static SortingType register(String name, Function<Island, Number> valueFunction, Comparator<Island> comparator) {
        SortingType.register(name, valueFunction, comparator, false);
        return SortingType.getByName(name);
    }

    public static SortingType getGlobalWarpsSortingType() {
        return GLOBAL_WARPS_SORTING;
    }

    public static SortingType getTopIslandsSortingType() {
        return TOP_ISLANDS_SORTING;
    }

    public static SortingType getVisitIslandsSortingType() {
        return VISIT_ISLANDS_SORTING;
    }

    private static void registerListeners(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, SortingTypes::onSettingsUpdate);
    }

    private static void onSettingsUpdate() {
        String globalWarpsOrder = getGlobalWarpsSortingTypeName();
        String topIslandsOrder = SuperiorSkyblockPlugin.getPlugin().getSettings().getIslandTopOrder();
        String visitIslandsOrder = getVisitIslandsSortingTypeName();

        GLOBAL_WARPS_SORTING = resolveByName(globalWarpsOrder);
        TOP_ISLANDS_SORTING = resolveByName(topIslandsOrder);
        VISIT_ISLANDS_SORTING = resolveByName(visitIslandsOrder);
    }

    private static SortingType resolveByName(String name) {
        return SortingType.getByName(name);
    }

    private static String getGlobalWarpsSortingTypeName() {
        PluginModule pluginModule = SuperiorSkyblockPlugin.getPlugin().getModules().getModule("warps");

        if (pluginModule instanceof WarpsModule) {
            WarpsModule warpsModule = (WarpsModule) pluginModule;

            return warpsModule.getConfiguration().getMenusGlobalWarpsOrder();
        }

        return "WORTH";
    }

    private static String getVisitIslandsSortingTypeName() {
        PluginModule pluginModule = SuperiorSkyblockPlugin.getPlugin().getModules().getModule("visit");

        if (pluginModule instanceof VisitModule) {
            VisitModule visitModule = (VisitModule) pluginModule;

            return visitModule.getConfiguration().getMenusVisitIslandsOrder();
        }

        return "WORTH";
    }

}
