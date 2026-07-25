package com.bgsoftware.superiorskyblock.island.upgrade;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.UpgradesManager;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.island.upgrade.container.UpgradesContainer;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Locale;

public class UpgradesManagerImpl extends Manager implements UpgradesManager {

    private final UpgradesContainer upgradesContainer;

    private String ALL_UPGRADE_NAMES;

    public UpgradesManagerImpl(SuperiorSkyblockPlugin plugin, UpgradesContainer upgradesContainer) {
        super(plugin);
        this.upgradesContainer = upgradesContainer;
    }

    @Override
    public void loadData() {
        // Data is loaded later.
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
        ALL_UPGRADE_NAMES = null;
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

    public void clearUpgrades() {
        this.upgradesContainer.clearUpgrades();
    }

    public String getUpgradesNames() {
        if (ALL_UPGRADE_NAMES == null) {
            ALL_UPGRADE_NAMES = Formatters.COMMA_FORMATTER.format(plugin.getUpgrades().getUpgrades().stream()
                    .map(upgrade -> upgrade.getName().toLowerCase(Locale.ENGLISH)));
        }

        return ALL_UPGRADE_NAMES;
    }

}
