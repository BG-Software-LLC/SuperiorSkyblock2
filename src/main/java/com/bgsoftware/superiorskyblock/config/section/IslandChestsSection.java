package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;

public class IslandChestsSection extends SettingsContainerHolder implements SettingsManager.IslandChests {

    @Override
    public String getChestTitle() {
        return getContainer().islandChestTitle;
    }

    @Override
    public int getDefaultPages() {
        return getContainer().islandChestsDefaultPage;
    }

    @Override
    public int getDefaultSize() {
        return getContainer().islandChestsDefaultSize;
    }

}
