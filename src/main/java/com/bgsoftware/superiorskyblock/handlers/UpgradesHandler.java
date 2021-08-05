package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import com.bgsoftware.superiorskyblock.api.handlers.UpgradesManager;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import com.bgsoftware.superiorskyblock.upgrades.DefaultUpgrade;
import com.bgsoftware.superiorskyblock.upgrades.SUpgrade;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class UpgradesHandler extends AbstractHandler implements UpgradesManager {

    private final Map<String, SUpgrade> upgrades = new HashMap<>();
    private final Map<String, UpgradeCostLoader> upgradeCostLoaders = new HashMap<>();

    public UpgradesHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
    }

    @Override
    public void loadData(){
    }

    @Override
    public SUpgrade getUpgrade(String upgradeName){
        Preconditions.checkNotNull(upgradeName, "upgradeName parameter cannot be null.");
        return upgrades.get(upgradeName.toLowerCase());
    }

    @Override
    public SUpgrade getUpgrade(int slot){
        return upgrades.values().stream().filter(upgrade -> upgrade.getMenuSlot() == slot).findFirst().orElse(null);
    }

    @Override
    public Upgrade getDefaultUpgrade() {
        return DefaultUpgrade.getInstance();
    }

    @Override
    public boolean isUpgrade(String upgradeName){
        Preconditions.checkNotNull(upgradeName, "upgradeName parameter cannot be null.");
        return upgrades.containsKey(upgradeName.toLowerCase());
    }

    @Override
    public Collection<Upgrade> getUpgrades() {
        return Collections.unmodifiableCollection(upgrades.values());
    }

    @Override
    public void registerUpgradeCostLoader(String id, UpgradeCostLoader costLoader) {
        id = id.toLowerCase();
        Preconditions.checkArgument(!upgradeCostLoaders.containsKey(id), "A loader with the id " + id + " already exists.");
        upgradeCostLoaders.put(id, costLoader);
    }

    @Override
    public Collection<UpgradeCostLoader> getUpgradesCostLoaders() {
        return Collections.unmodifiableCollection(upgradeCostLoaders.values());
    }

    @Nullable
    @Override
    public UpgradeCostLoader getUpgradeCostLoader(String id) {
        Preconditions.checkNotNull(id, "id parameter cannot be null.");
        return upgradeCostLoaders.get(id.toLowerCase());
    }

    public void loadUpgrade(SUpgrade upgrade){
        this.upgrades.put(upgrade.getName().toLowerCase(), upgrade);
    }

}
