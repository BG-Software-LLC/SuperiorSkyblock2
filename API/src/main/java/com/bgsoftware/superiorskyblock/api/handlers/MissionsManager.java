package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.List;

public interface MissionsManager {

    Mission getMission(String name);

    List<Mission> getAllMissions();

    List<Mission> getPlayerMissions();

    List<Mission> getIslandMissions();

    boolean hasCompleted(SuperiorPlayer superiorPlayer, Mission mission);

    void rewardMission(Mission mission, SuperiorPlayer superiorPlayer, boolean checkAutoReward);

}
