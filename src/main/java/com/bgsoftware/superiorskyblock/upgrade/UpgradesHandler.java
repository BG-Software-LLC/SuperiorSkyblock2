package com.bgsoftware.superiorskyblock.upgrade;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.UpgradesManager;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import com.bgsoftware.superiorskyblock.handler.AbstractHandler;
import com.bgsoftware.superiorskyblock.upgrade.container.UpgradesContainer;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.Collection;

public final class UpgradesHandler extends AbstractHandler implements UpgradesManager {

    private final UpgradesContainer upgradesContainer;

    public UpgradesHandler(SuperiorSkyblockPlugin plugin, UpgradesContainer upgradesContainer) {
        super(plugin);
        this.upgradesContainer = upgradesContainer;
    }

    @Override
    public void loadData() {
    }

    @Override
    public Upgrade getUpgrade(String upgradeName) {
        Preconditions.checkNotNull(upgradeName, "upgradeName parameter cannot be null.");
        return this.upgradesContainer.getUpgrade(upgradeName);
    }

    @Override
    public Upgrade getUpgrade(int slot) {
        return this.upgradesContainer.getUpgrade(slot);
    }

    @Override
    public void addUpgrade(Upgrade upgrade) {
        this.upgradesContainer.addUpgrade(upgrade);
    }

    @Override
    public Upgrade getDefaultUpgrade() {
        return DefaultUpgrade.getInstance();
    }

    @Override
    public boolean isUpgrade(String upgradeName) {
        Preconditions.checkNotNull(upgradeName, "upgradeName parameter cannot be null.");
        return getUpgrade(upgradeName) != null;
    }

    @Override
    public Collection<Upgrade> getUpgrades() {
        return this.upgradesContainer.getUpgrades();
    }

    @Override
    public void registerUpgradeCostLoader(String id, UpgradeCostLoader costLoader) {
        Preconditions.checkNotNull(id, "id parameter cannot be null.");
        Preconditions.checkNotNull(costLoader, "costLoader parameter cannot be null.");
        this.upgradesContainer.registerUpgradeCostLoader(id, costLoader);
    }

    @Override
    public Collection<UpgradeCostLoader> getUpgradesCostLoaders() {
        return this.upgradesContainer.getUpgradesCostLoaders();
    }

    @Nullable
    @Override
    public UpgradeCostLoader getUpgradeCostLoader(String id) {
        Preconditions.checkNotNull(id, "id parameter cannot be null.");
        return this.upgradesContainer.getUpgradeCostLoader(id);
    }

}
