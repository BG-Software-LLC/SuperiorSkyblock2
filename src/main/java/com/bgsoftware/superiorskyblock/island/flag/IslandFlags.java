package com.bgsoftware.superiorskyblock.island.flag;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Locale;

public class IslandFlags {

    public static final IslandFlag ALWAYS_DAY = register("ALWAYS_DAY", IslandFlag.Config.newBuilder()
            .setEnableCallback(island -> onDateSettingsEnableCallback(island, 0))
            .setDisableCallback(IslandFlags::onDateSettingsDisableCallback)
            .setConflictingIslandFlags("ALWAYS_MIDDLE_DAY", "ALWAYS_NIGHT", "ALWAYS_MIDDLE_NIGHT")
            .build());
    public static final IslandFlag ALWAYS_MIDDLE_DAY = register("ALWAYS_MIDDLE_DAY", IslandFlag.Config.newBuilder()
            .setEnableCallback(island -> onDateSettingsEnableCallback(island, 6000))
            .setDisableCallback(IslandFlags::onDateSettingsDisableCallback)
            .setConflictingIslandFlags("ALWAYS_DAY", "ALWAYS_NIGHT", "ALWAYS_MIDDLE_NIGHT")
            .build());
    public static final IslandFlag ALWAYS_NIGHT = register("ALWAYS_NIGHT", IslandFlag.Config.newBuilder()
            .setEnableCallback(island -> onDateSettingsEnableCallback(island, 14000))
            .setDisableCallback(IslandFlags::onDateSettingsDisableCallback)
            .setConflictingIslandFlags("ALWAYS_DAY", "ALWAYS_MIDDLE_DAY", "ALWAYS_MIDDLE_NIGHT")
            .build());
    public static final IslandFlag ALWAYS_MIDDLE_NIGHT = register("ALWAYS_MIDDLE_NIGHT", IslandFlag.Config.newBuilder()
            .setEnableCallback(island -> onDateSettingsEnableCallback(island, 18000))
            .setDisableCallback(IslandFlags::onDateSettingsDisableCallback)
            .setConflictingIslandFlags("ALWAYS_DAY", "ALWAYS_MIDDLE_DAY", "ALWAYS_NIGHT")
            .build());
    public static final IslandFlag ALWAYS_RAIN = register("ALWAYS_RAIN", IslandFlag.Config.newBuilder()
            .setEnableCallback(island -> onWeatherSettingsEnableCallback(island, WeatherType.DOWNFALL))
            .setDisableCallback(IslandFlags::onWeatherSettingsDisableCallback)
            .setConflictingIslandFlags("ALWAYS_SHINY")
            .build());
    public static final IslandFlag ALWAYS_SHINY = register("ALWAYS_SHINY", IslandFlag.Config.newBuilder()
            .setEnableCallback(island -> onWeatherSettingsEnableCallback(island, WeatherType.CLEAR))
            .setDisableCallback(IslandFlags::onWeatherSettingsDisableCallback)
            .setConflictingIslandFlags("ALWAYS_RAIN")
            .build());
    public static final IslandFlag CREEPER_EXPLOSION = register("CREEPER_EXPLOSION");
    public static final IslandFlag CROPS_GROWTH = register("CROPS_GROWTH");
    public static final IslandFlag EGG_LAY = register("EGG_LAY");
    public static final IslandFlag ENDERMAN_GRIEF = register("ENDERMAN_GRIEF");
    public static final IslandFlag FIRE_SPREAD = register("FIRE_SPREAD");
    public static final IslandFlag GHAST_FIREBALL = register("GHAST_FIREBALL");
    public static final IslandFlag LAVA_FLOW = register("LAVA_FLOW");
    public static final IslandFlag PVP = register("PVP", IslandFlag.Config.newBuilder()
            .setEnableCallback(IslandFlags::onPvPSettingsEnableCallback)
            .build());
    public static final IslandFlag TNT_EXPLOSION = register("TNT_EXPLOSION");
    public static final IslandFlag TREE_GROWTH = register("TREE_GROWTH");
    public static final IslandFlag WATER_FLOW = register("WATER_FLOW");
    public static final IslandFlag WITHER_EXPLOSION = register("WITHER_EXPLOSION");

    private static String ALL_FLAG_NAMES;
    private static int KNOWN_FLAGS_COUNT;

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private IslandFlags() {

    }

    private static void onDateSettingsEnableCallback(Island island, int time) {
        island.getAllPlayersInside().forEach(superiorPlayer -> {
            Player player = superiorPlayer.asPlayer();
            if (player != null)
                player.setPlayerTime(time, false);
        });
    }

    private static void onDateSettingsDisableCallback(Island island) {
        island.getAllPlayersInside().forEach(superiorPlayer -> {
            Player player = superiorPlayer.asPlayer();
            if (player != null)
                player.resetPlayerTime();
        });
    }

    private static void onWeatherSettingsEnableCallback(Island island, WeatherType weatherType) {
        island.getAllPlayersInside().forEach(superiorPlayer -> {
            Player player = superiorPlayer.asPlayer();
            if (player != null)
                player.setPlayerWeather(weatherType);
        });
    }

    private static void onWeatherSettingsDisableCallback(Island island) {
        island.getAllPlayersInside().forEach(superiorPlayer -> {
            Player player = superiorPlayer.asPlayer();
            if (player != null)
                player.resetPlayerWeather();
        });
    }

    private static void onPvPSettingsEnableCallback(Island island) {
        if (plugin.getSettings().isTeleportOnPvPEnable()) {
            island.getIslandVisitors().forEach(superiorPlayer -> {
                superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                Message.ISLAND_GOT_PVP_ENABLED_WHILE_INSIDE.send(superiorPlayer);
            });
        }
    }

    public static void registerFlags() {
        // Do nothing, only trigger all the register calls
    }

    public static String getFlagsNames() {
        if (ALL_FLAG_NAMES == null || KNOWN_FLAGS_COUNT != IslandFlag.values().size()) {
            ALL_FLAG_NAMES = Formatters.COMMA_FORMATTER.format(IslandFlag.values().stream()
                    .sorted(Comparator.comparing(IslandFlag::getName))
                    .map(islandFlag -> islandFlag.getName().toLowerCase(Locale.ENGLISH)));
            KNOWN_FLAGS_COUNT = IslandFlag.values().size();
        }

        return ALL_FLAG_NAMES;
    }

    private static IslandFlag register(String name) {
        return register(name, null);
    }

    private static IslandFlag register(String name, @Nullable IslandFlag.Config config) {
        IslandFlag.register(name, config);
        return IslandFlag.getByName(name);
    }

}
