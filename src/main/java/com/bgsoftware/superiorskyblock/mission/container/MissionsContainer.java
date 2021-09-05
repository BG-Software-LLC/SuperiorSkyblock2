package com.bgsoftware.superiorskyblock.mission.container;

import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.mission.MissionData;

import javax.annotation.Nullable;
import java.util.List;

public interface MissionsContainer {

    void addMission(Mission<?> mission);

    @Nullable
    Mission<?> getMission(String name);

    List<Mission<?>> getAllMissions();

    List<Mission<?>> getPlayerMissions();

    List<Mission<?>> getIslandMissions();

    void addMissionData(MissionData missionData);

    @Nullable
    MissionData getMissionData(Mission<?> mission);

    void addMissionCategory(MissionCategory missionCategory);

    @Nullable
    MissionCategory getMissionCategory(String name);

    List<MissionCategory> getMissionCategories();

}
