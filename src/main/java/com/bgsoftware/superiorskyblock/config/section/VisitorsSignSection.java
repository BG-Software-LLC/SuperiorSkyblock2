package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;

public class VisitorsSignSection extends SettingsContainerHolder implements SettingsManager.VisitorsSign {

    @Override
    @Deprecated
    public boolean isRequiredForVisit() {
        return BuiltinModules.VISIT.getConfiguration().isSignsRequiredForVisit();
    }

    @Override
    @Deprecated
    public String getLine() {
        return BuiltinModules.VISIT.getConfiguration().getSignsCreateLine();
    }

    @Override
    @Deprecated
    public String getActive() {
        return BuiltinModules.VISIT.getConfiguration().getSignsActiveLine();
    }

    @Override
    @Deprecated
    public String getInactive() {
        return BuiltinModules.VISIT.getConfiguration().getSignsInactiveLine();
    }

    @Override
    @Deprecated
    public String getDescriptionLineFormat() {
        return BuiltinModules.VISIT.getConfiguration().getDescriptionsLineFormat();
    }

}
