package com.bgsoftware.superiorskyblock.island.top;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;

import java.util.Comparator;
import java.util.Objects;

public class SortingTypes {

    public static final SortingType BY_WORTH = register("WORTH", SortingComparators.WORTH_COMPARATOR, false);
    public static final SortingType BY_LEVEL = register("LEVEL", SortingComparators.LEVEL_COMPARATOR, false);
    public static final SortingType BY_RATING = register("RATING", SortingComparators.RATING_COMPARATOR, false);
    public static final SortingType BY_PLAYERS = register("PLAYERS", SortingComparators.PLAYERS_COMPARATOR, false);

    private static volatile SortingType ISLAND_TOP_SORTING = resolveByName(
            SuperiorSkyblockPlugin.getPlugin().getSettings().getIslandTopOrder());
    private static volatile SortingType GLOBAL_WARPS_SORTING = resolveByName(
            SuperiorSkyblockPlugin.getPlugin().getSettings().getGlobalWarpsOrder());

    private SortingTypes() {
    }

    public static void registerSortingTypes() {
        // Do nothing, only trigger all the register calls
    }

    private static SortingType register(String name, Comparator<Island> comparator, boolean handleEqualsIslands) {
        SortingType.register(name, comparator, handleEqualsIslands);
        return Objects.requireNonNull(SortingType.getByName(name), "SortingType non enregistré: " + name);
    }

    public static SortingType getIslandTopSorting() {
        return ISLAND_TOP_SORTING;
    }

    public static SortingType getGlobalWarpsSorting() {
        return GLOBAL_WARPS_SORTING;
    }

    public static void registerListeners(PluginEventsDispatcher dispatcher) {
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

