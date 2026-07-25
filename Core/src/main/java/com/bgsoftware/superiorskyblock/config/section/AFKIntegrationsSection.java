package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;

public class AFKIntegrationsSection extends SettingsContainerHolder implements SettingsManager.AFKIntegrations {

    @Override
    public boolean isDisableRedstone() {
        return getContainer().disableRedstoneAFK;
    }

    @Override
    public boolean isDisableSpawning() {
        return getContainer().disableSpawningAFK;
    }

}
