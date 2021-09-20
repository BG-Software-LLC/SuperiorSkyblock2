package com.bgsoftware.superiorskyblock.upgrade.container;

import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DefaultUpgradesContainer implements UpgradesContainer {

    private final Map<String, Upgrade> upgrades = new HashMap<>();
    private final Map<String, UpgradeCostLoader> upgradeCostLoaders = new HashMap<>();

    @Nullable
    @Override
    public Upgrade getUpgrade(String upgradeName) {
        return this.upgrades.get(upgradeName.toLowerCase());
    }

    @Nullable
    @Override
    public Upgrade getUpgrade(int slot) {
        return this.upgrades.values().stream()
                .filter(upgrade -> upgrade.getSlot() == slot)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<Upgrade> getUpgrades() {
        return Collections.unmodifiableCollection(this.upgrades.values());
    }

    @Override
    public void registerUpgradeCostLoader(String id, UpgradeCostLoader costLoader) {
        this.upgradeCostLoaders.put(id.toLowerCase(), costLoader);
    }

    @Override
    public Collection<UpgradeCostLoader> getUpgradesCostLoaders() {
        return Collections.unmodifiableCollection(this.upgradeCostLoaders.values());
    }

    @Override
    public UpgradeCostLoader getUpgradeCostLoader(String id) {
        return this.upgradeCostLoaders.get(id.toLowerCase());
    }

    @Override
    public void addUpgrade(Upgrade upgrade) {
        this.upgrades.put(upgrade.getName().toLowerCase(), upgrade);
    }

}
