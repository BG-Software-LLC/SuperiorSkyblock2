package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;

public class VisitorsSignSection extends SettingsContainerHolder implements SettingsManager.VisitorsSign {

    @Override
    public boolean isRequiredForVisit() {
        return getContainer().visitorsSignRequiredForVisit;
    }

    @Override
    public String getLine() {
        return getContainer().visitorsSignLine;
    }

    @Override
    public String getActive() {
        return getContainer().visitorsSignActive;
    }

    @Override
    public String getInactive() {
        return getContainer().visitorsSignInactive;
    }

}
