package com.bgsoftware.superiorskyblock.mission;

import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;

import java.util.Collections;
import java.util.List;

public class SMissionCategory implements MissionCategory {

    private final String name;
    private final int slot;
    private final List<Mission<?>> missions;

    public SMissionCategory(String name, int slot, List<Mission<?>> missions) {
        this.name = name;
        this.slot = slot;
        this.missions = missions;
        missions.forEach(mission -> mission.setMissionCategory(this));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public List<Mission<?>> getMissions() {
        return Collections.unmodifiableList(this.missions);
    }

}
