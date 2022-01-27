package com.bgsoftware.superiorskyblock.database.cache;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.lang.PlayerLocales;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class CachedPlayerInfo {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public boolean toggledPanel = plugin.getSettings().isDefaultToggledPanel();
    public boolean islandFly = plugin.getSettings().isDefaultIslandFly();
    public BorderColor borderColor = BorderColor.safeValue(plugin.getSettings().getDefaultBorderColor(), BorderColor.BLUE);
    public Locale userLocale = PlayerLocales.getDefaultLocale();
    public boolean worldBorderEnabled = plugin.getSettings().isDefaultWorldBorder();
    public Map<Mission<?>, Integer> completedMissions = new HashMap<>();

}
