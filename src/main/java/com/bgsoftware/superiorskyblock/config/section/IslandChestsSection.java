package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainer;

public class IslandChestsSection implements SettingsManager.IslandChests {

    private final SettingsContainer container;

    public IslandChestsSection(SettingsContainer container) {
        this.container = container;
    }

    @Override
    public String getChestTitle() {
        return this.container.islandChestTitle;
    }

    @Override
    public int getDefaultPages() {
        return this.container.islandChestsDefaultPage;
    }

    @Override
    public int getDefaultSize() {
        return this.container.islandChestsDefaultSize;
    }

}
