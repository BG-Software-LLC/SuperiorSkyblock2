package com.bgsoftware.superiorskyblock.missions.common.tracker;

import com.bgsoftware.superiorskyblock.missions.common.requirements.Requirements;

import java.util.HashMap;

public class RawDataTracker extends DataTracker<String, Requirements> {

    public RawDataTracker() {
        super(new HashMap<>());
    }

}
