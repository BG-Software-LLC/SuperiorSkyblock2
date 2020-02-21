package com.bgsoftware.superiorskyblock.api.upgrades;

public interface Upgrade {

    String getName();

    UpgradeLevel getUpgradeLevel(int level);

    int getMaxUpgradeLevel();

}
