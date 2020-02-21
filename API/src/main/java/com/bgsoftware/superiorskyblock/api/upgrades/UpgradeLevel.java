package com.bgsoftware.superiorskyblock.api.upgrades;

import com.bgsoftware.superiorskyblock.api.key.Key;

import java.util.List;

public interface UpgradeLevel {

    int getLevel();

    double getPrice();

    List<String> getCommands();

    String getPermission();

    double getCropGrowth();

    double getSpawnerRates();

    double getMobDrops();

    int getBlockLimit(Key key);

    int getExactBlockLimit(Key key);

    int getTeamLimit();

    int getWarpsLimit();

    int getBorderSize();

    int getGeneratorAmount(Key key);

}
