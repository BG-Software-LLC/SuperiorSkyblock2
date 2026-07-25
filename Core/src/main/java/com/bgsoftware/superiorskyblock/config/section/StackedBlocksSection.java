package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StackedBlocksSection extends SettingsContainerHolder implements SettingsManager.StackedBlocks {
    private final DepositMenu depositMenu = new DepositMenuSection();

    @Override
    public boolean isEnabled() {
        return getContainer().stackedBlocksEnabled;
    }

    @Override
    public String getCustomName() {
        return getContainer().stackedBlocksName;
    }

    @Override
    public List<String> getDisabledWorlds() {
        return getContainer().stackedBlocksDisabledWorlds;
    }

    @Override
    public Set<Key> getWhitelisted() {
        return getContainer().whitelistedStackedBlocks;
    }

    @Override
    public Map<Key, Integer> getLimits() {
        return getContainer().stackedBlocksLimits;
    }

    @Override
    public boolean isAutoCollect() {
        return getContainer().stackedBlocksAutoPickup;
    }

    @Override
    public DepositMenu getDepositMenu() {
        return this.depositMenu;
    }

    private class DepositMenuSection implements DepositMenu {

        @Override
        public boolean isEnabled() {
            return getContainer().stackedBlocksMenuEnabled;
        }

        @Override
        public String getTitle() {
            return getContainer().stackedBlocksMenuTitle;
        }

    }

}
