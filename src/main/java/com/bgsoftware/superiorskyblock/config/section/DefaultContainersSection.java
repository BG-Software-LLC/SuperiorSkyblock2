package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import org.bukkit.event.inventory.InventoryType;

public class DefaultContainersSection extends SettingsContainerHolder implements SettingsManager.DefaultContainers {

    @Override
    public boolean isEnabled() {
        return getContainer().defaultContainersEnabled;
    }

    public ListTag getContents(InventoryType inventoryType) {
        return getContainer().defaultContainersContents.get(inventoryType);
    }

}
