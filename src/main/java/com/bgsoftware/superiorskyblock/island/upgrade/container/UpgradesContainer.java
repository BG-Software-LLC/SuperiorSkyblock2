package com.bgsoftware.superiorskyblock.island.upgrade.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;

import java.util.Collection;

public interface UpgradesContainer {

    @Nullable
    Upgrade getUpgrade(String upgradeName);

    @Nullable
    Upgrade getUpgrade(int slot);

    Collection<Upgrade> getUpgrades();

    void registerUpgradeCostLoader(String id, UpgradeCostLoader costLoader);

    Collection<UpgradeCostLoader> getUpgradesCostLoaders();

    UpgradeCostLoader getUpgradeCostLoader(String id);

    void addUpgrade(Upgrade upgrade);

}
