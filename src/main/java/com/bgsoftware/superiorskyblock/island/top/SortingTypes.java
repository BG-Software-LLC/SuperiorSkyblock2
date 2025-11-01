package com.bgsoftware.superiorskyblock.island.top;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;

import java.util.Comparator;

public class SortingTypes {

    public static final SortingType BY_WORTH = register("WORTH", SortingComparators.WORTH_COMPARATOR, false);
    public static final SortingType BY_LEVEL = register("LEVEL", SortingComparators.LEVEL_COMPARATOR, false);
    public static final SortingType BY_RATING = register("RATING", SortingComparators.RATING_COMPARATOR, false);
    public static final SortingType BY_PLAYERS = register("PLAYERS", SortingComparators.PLAYERS_COMPARATOR, false);

    private static volatile SortingType ISLAND_TOP_SORTING;
    private static volatile SortingType GLOBAL_WARPS_SORTING;

    private SortingTypes() {
    }

    public static void registerSortingTypes(SuperiorSkyblockPlugin plugin) {
        // We actually register the settings update listener in here, as otherwise it causes errors
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/2752
        registerListeners(plugin.getPluginEventsDispatcher());
    }

    private static SortingType register(String name, Comparator<Island> comparator, boolean handleEqualsIslands) {
        SortingType.register(name, comparator, handleEqualsIslands);
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

