package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;

import java.util.Set;

public class MobDropsSection extends SettingsContainerHolder implements SettingsManager.MobDrops {

    @Override
    public boolean isOnlyPlayerKills() {
        return getContainer().mobDropsOnlyPlayerKills;
    }

    @Override
    public Set<Key> getWhitelistedItems() {
        return getContainer().mobDropsWhitelistedItems;
    }

    @Override
    public Set<Key> getBlacklistedItems() {
        return getContainer().mobDropsBlacklistedItems;
    }

    @Override
    public Set<Key> getWhitelistedEntities() {
        return getContainer().mobDropsWhitelistedEntities;
    }

    @Override
    public Set<Key> getBlacklistedEntities() {
        return getContainer().mobDropsBlacklistedEntities;
    }

}