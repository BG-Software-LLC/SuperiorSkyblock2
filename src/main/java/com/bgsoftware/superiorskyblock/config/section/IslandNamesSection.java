package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainer;

import java.util.List;

public class IslandNamesSection implements SettingsManager.IslandNames {

    private final SettingsContainer container;

    public IslandNamesSection(SettingsContainer container) {
        this.container = container;
    }

    @Override
    public boolean isRequiredForCreation() {
        return this.container.islandNamesRequiredForCreation;
    }

    @Override
    public int getMaxLength() {
        return this.container.islandNamesMaxLength;
    }

    @Override
    public int getMinLength() {
        return this.container.islandNamesMinLength;
    }

    @Override
    public List<String> getFilteredNames() {
        return this.container.filteredIslandNames;
    }

    @Override
    public boolean isColorSupport() {
        return this.container.islandNamesColorSupport;
    }

    @Override
    public boolean isIslandTop() {
        return this.container.islandNamesIslandTop;
    }

    @Override
    public boolean isPreventPlayerNames() {
        return this.container.islandNamesPreventPlayerNames;
    }

}
