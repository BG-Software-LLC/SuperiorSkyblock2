package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainer;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import org.bukkit.event.inventory.InventoryType;

public class DefaultContainersSection implements SettingsManager.DefaultContainers {

    private final SettingsContainer container;

    public DefaultContainersSection(SettingsContainer container) {
        this.container = container;
    }

    @Override
    public boolean isEnabled() {
        return this.container.defaultContainersEnabled;
    }

    public ListTag getContents(InventoryType inventoryType) {
        return this.container.defaultContainersContents.get(inventoryType);
    }

}
