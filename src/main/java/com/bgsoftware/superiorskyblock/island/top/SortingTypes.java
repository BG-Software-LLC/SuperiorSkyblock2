package com.bgsoftware.superiorskyblock.island.top;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;

import java.util.Comparator;
import java.util.function.Function;

public class SortingTypes {

    public static final SortingType BY_WORTH = register("WORTH", Island::getWorth, SortingComparators.WORTH_COMPARATOR);
    public static final SortingType BY_LEVEL = register("LEVEL", Island::getIslandLevel, SortingComparators.LEVEL_COMPARATOR);
    public static final SortingType BY_RATING = register("RATING", Island::getTotalRating, SortingComparators.RATING_COMPARATOR);
    public static final SortingType BY_PLAYERS = register("PLAYERS", island -> island.getAllPlayersInside().size(), SortingComparators.PLAYERS_COMPARATOR);
    public static final SortingType BY_BANK = register("BANK", island -> island.getIslandBank().getBalance(), SortingComparators.BANK_COMPARATOR);

    private static volatile SortingType ISLAND_TOP_SORTING;
    private static volatile SortingType GLOBAL_WARPS_SORTING;

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

    public static SortingType getIslandTopSorting() {
        return ISLAND_TOP_SORTING;
    }

    public static SortingType getGlobalWarpsSorting() {
        return GLOBAL_WARPS_SORTING;
    }

    private static void registerListeners(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, SortingTypes::onSettingsUpdate);
    }

    private static void onSettingsUpdate() {
        String topOrder = SuperiorSkyblockPlugin.getPlugin().getSettings().getIslandTopOrder();
        String warpsOrder = SuperiorSkyblockPlugin.getPlugin().getSettings().getGlobalWarpsOrder();

        ISLAND_TOP_SORTING = resolveByName(topOrder);
        GLOBAL_WARPS_SORTING = resolveByName(warpsOrder);
    }

    private static SortingType resolveByName(String name) {
        return SortingType.getByName(name);
    }

}
