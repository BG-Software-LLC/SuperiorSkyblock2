package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;

public class VoidTeleportSection extends SettingsContainerHolder implements SettingsManager.VoidTeleport {

    @Override
    public boolean isMembers() {
        return getContainer().voidTeleportMembers;
    }

    @Override
    public boolean isVisitors() {
        return getContainer().voidTeleportVisitors;
    }

}
