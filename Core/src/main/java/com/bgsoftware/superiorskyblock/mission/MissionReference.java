package com.bgsoftware.superiorskyblock.mission;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class MissionReference {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final String missionName;
    private WeakReference<Mission<?>> reference;

    public MissionReference(Mission<?> mission) {
        this.missionName = mission.getName();
        this.reference = new WeakReference<>(mission);
    }

    @Nullable
    public Mission<?> getMission() {
        Mission<?> mission = this.reference.get();

        if (mission == null) {
            mission = plugin.getMissions().getMission(this.missionName);
            if (mission != null)
                this.reference = new WeakReference<>(mission);
        }

        return mission;
    }

    public boolean isValid() {
        return this.getMission() != null;
    }

    @Override
    public int hashCode() {
        return this.missionName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionReference that = (MissionReference) o;
        return that.missionName.hashCode() == that.missionName.hashCode() &&
                Objects.equals(missionName, that.missionName);
    }

}
