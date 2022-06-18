package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainer;

public class AFKIntegrationsSection implements SettingsManager.AFKIntegrations {

    private final SettingsContainer container;

    public AFKIntegrationsSection(SettingsContainer container) {
        this.container = container;
    }

    @Override
    public boolean isDisableRedstone() {
        return this.container.disableRedstoneAFK;
    }

    @Override
    public boolean isDisableSpawning() {
        return this.container.disableSpawningAFK;
    }

}
