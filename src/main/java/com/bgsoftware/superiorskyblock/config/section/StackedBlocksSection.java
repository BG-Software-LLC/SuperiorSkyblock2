package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.config.SettingsContainer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StackedBlocksSection implements SettingsManager.StackedBlocks {

    private final SettingsContainer container;
    private final DepositMenu depositMenu = new DepositMenuSection();

    public StackedBlocksSection(SettingsContainer container) {
        this.container = container;
    }

    @Override
    public boolean isEnabled() {
        return this.container.stackedBlocksEnabled;
    }

    @Override
    public String getCustomName() {
        return this.container.stackedBlocksName;
    }

    @Override
    public List<String> getDisabledWorlds() {
        return this.container.stackedBlocksDisabledWorlds;
    }

    @Override
    public Set<Key> getWhitelisted() {
        return this.container.whitelistedStackedBlocks;
    }

    @Override
    public Map<Key, Integer> getLimits() {
        return this.container.stackedBlocksLimits;
    }

    @Override
    public boolean isAutoCollect() {
        return this.container.stackedBlocksAutoPickup;
    }

    @Override
    public DepositMenu getDepositMenu() {
        return this.depositMenu;
    }

    private class DepositMenuSection implements DepositMenu {

        @Override
        public boolean isEnabled() {
            return container.stackedBlocksMenuEnabled;
        }

        @Override
        public String getTitle() {
            return container.stackedBlocksMenuTitle;
        }

    }

}
