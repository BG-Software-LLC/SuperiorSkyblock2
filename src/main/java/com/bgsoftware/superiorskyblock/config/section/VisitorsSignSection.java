package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainer;

public class VisitorsSignSection implements SettingsManager.VisitorsSign {

    private final SettingsContainer container;

    public VisitorsSignSection(SettingsContainer container) {
        this.container = container;
    }

    @Override
    public boolean isRequiredForVisit() {
        return this.container.visitorsSignRequiredForVisit;
    }

    @Override
    public String getLine() {
        return this.container.visitorsSignLine;
    }

    @Override
    public String getActive() {
        return this.container.visitorsSignActive;
    }

    @Override
    public String getInactive() {
        return this.container.visitorsSignInactive;
    }

}
