package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;
import org.bukkit.GameMode;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;

public class IslandPreviewsSection extends SettingsContainerHolder implements SettingsManager.IslandPreviews {

    @Override
    public GameMode getGameMode() {
        return getContainer().islandPreviewsGameMode;
    }

    @Override
    public int getMaxDistance() {
        return getContainer().islandPreviewsMaxDistance;
    }

    @Override
    public List<String> getBlockedCommands() {
        return getContainer().islandPreviewsBlockedCommands;
    }

    @Override
    public Map<String, Location> getLocations() {
        return getContainer().islandPreviewsLocations;
    }

}
