package com.bgsoftware.superiorskyblock.island.upgrade.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DefaultUpgradesContainer implements UpgradesContainer {

    private final Map<String, Upgrade> upgrades = new HashMap<>();
    private final Map<String, UpgradeCostLoader> upgradeCostLoaders = new HashMap<>();

    @Nullable
    @Override
    public Upgrade getUpgrade(String upgradeName) {
        return this.upgrades.get(upgradeName.toLowerCase(Locale.ENGLISH));
    }

    @Nullable
    @Override
    public Upgrade getUpgrade(int slot) {
        return this.upgrades.values().stream()
                .filter(upgrade -> upgrade.isSlot(slot))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<Upgrade> getUpgrades() {
        return new SequentialListBuilder<Upgrade>().build(this.upgrades.values());
    }

    @Override
    public void registerUpgradeCostLoader(String id, UpgradeCostLoader costLoader) {
        this.upgradeCostLoaders.put(id.toLowerCase(Locale.ENGLISH), costLoader);
    }

    @Override
    public Collection<UpgradeCostLoader> getUpgradesCostLoaders() {
        return new SequentialListBuilder<UpgradeCostLoader>().build(this.upgradeCostLoaders.values());
    }

    @Override
    public UpgradeCostLoader getUpgradeCostLoader(String id) {
        return this.upgradeCostLoaders.get(id.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public void addUpgrade(Upgrade upgrade) {
        this.upgrades.put(upgrade.getName().toLowerCase(Locale.ENGLISH), upgrade);
    }

}
