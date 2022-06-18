package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainer;
import org.bukkit.configuration.ConfigurationSection;

public class IslandRolesSection implements SettingsManager.IslandRoles {

    private final SettingsContainer container;

    public IslandRolesSection(SettingsContainer container) {
        this.container = container;
    }

    @Override
    public ConfigurationSection getSection() {
        return this.container.islandRolesSection;
    }

}
