package com.bgsoftware.superiorskyblock.missions.common.tracker;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.missions.common.requirements.KeyRequirements;

public class KeyDataTracker extends DataTracker<Key, KeyRequirements> {

    public KeyDataTracker() {
        super(KeyMap.createKeyMap());
    }

}
