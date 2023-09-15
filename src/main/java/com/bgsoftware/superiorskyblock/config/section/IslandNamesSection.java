package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;

import java.util.List;

public class IslandNamesSection extends SettingsContainerHolder implements SettingsManager.IslandNames {

    @Override
    public boolean isRequiredForCreation() {
        return getContainer().islandNamesRequiredForCreation;
    }

    @Override
    public int getMaxLength() {
        return getContainer().islandNamesMaxLength;
    }

    @Override
    public int getMinLength() {
        return getContainer().islandNamesMinLength;
    }

    @Override
    public List<String> getFilteredNames() {
        return getContainer().filteredIslandNames;
    }

    @Override
    public boolean isColorSupport() {
        return getContainer().islandNamesColorSupport;
    }

    @Override
    public boolean isIslandTop() {
        return getContainer().islandNamesIslandTop;
    }

    @Override
    public boolean isPreventPlayerNames() {
        return getContainer().islandNamesPreventPlayerNames;
    }

}
