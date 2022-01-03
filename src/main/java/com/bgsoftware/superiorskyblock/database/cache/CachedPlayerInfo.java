package com.bgsoftware.superiorskyblock.database.cache;

import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.missions.Mission;

import java.util.Locale;
import java.util.Map;

public final class CachedPlayerInfo {

    public boolean toggledPanel;
    public boolean islandFly;
    public BorderColor borderColor;
    public Locale userLocale;
    public boolean worldBorderEnabled;
    public Map<Mission<?>, Integer> completedMissions;

}
