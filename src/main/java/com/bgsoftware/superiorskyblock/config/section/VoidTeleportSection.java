package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainer;

public class VoidTeleportSection implements SettingsManager.VoidTeleport {

    private final SettingsContainer container;

    public VoidTeleportSection(SettingsContainer container) {
        this.container = container;
    }

    @Override
    public boolean isMembers() {
        return this.container.voidTeleportMembers;
    }

    @Override
    public boolean isVisitors() {
        return this.container.voidTeleportVisitors;
    }

}
