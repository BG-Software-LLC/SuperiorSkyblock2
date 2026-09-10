package com.bgsoftware.superiorskyblock.missions.common.tracker;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.missions.common.requirements.KeyRequirements;

import java.util.Map;

public class KeyDataTracker extends DataTracker<Key, KeyRequirements> {

    public KeyDataTracker() {
        super(KeyMap.createKeyMap());
    }

    @Override
    protected Map<Key, Counter> createSnapshotMap() {
        return KeyMap.createKeyMap();
    }

}
