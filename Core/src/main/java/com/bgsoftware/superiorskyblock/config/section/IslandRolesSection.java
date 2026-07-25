package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;
import org.bukkit.configuration.ConfigurationSection;

public class IslandRolesSection extends SettingsContainerHolder implements SettingsManager.IslandRoles {

    @Override
    public ConfigurationSection getSection() {
        return getContainer().islandRolesSection;
    }

}
