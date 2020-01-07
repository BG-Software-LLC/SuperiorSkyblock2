package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.List;

public interface MissionsManager {

    /**
     * Get a mission by it's name.
     * @param name The name to check.
     * @return The mission with that name. May be null.
     */
    Mission getMission(String name);

    /**
     * Get a list of all the missions.
     */
    List<Mission> getAllMissions();

    /**
     * Get a list of all missions that are player missions.
     */
    List<Mission> getPlayerMissions();

    /**
     * Get a list of all missions that are island missions.
     */
    List<Mission> getIslandMissions();

    /**
     * Check whether or not the player has already completed the mission.
     * @param superiorPlayer The player to check.
     * @param mission The mission to check.
     * @return True if player has completed, otherwise false.
     */
    boolean hasCompleted(SuperiorPlayer superiorPlayer, Mission mission);

    /**
     * Check whether or not the player can complete the mission again.
     * @param superiorPlayer The player to check.
     * @param mission The mission to check.
     * @return True if player can complete, otherwise false.
     */
    boolean canCompleteAgain(SuperiorPlayer superiorPlayer, Mission mission);

    /**
     * Check whether or not a player can complete a mission.
     * @param superiorPlayer The player to check.
     * @param mission The mission to check.
     * @return True if player can complete, otherwise false.
     */
    boolean canComplete(SuperiorPlayer superiorPlayer, Mission mission);

    /**
     * Reward a player for completing a specific mission.
     * @param mission The mission that was completed.
     * @param superiorPlayer The player to reward.
     * @param checkAutoReward Whether or not the auto reward flag should be checked.
     */
    void rewardMission(Mission mission, SuperiorPlayer superiorPlayer, boolean checkAutoReward);

    /**
     * Reward a player for completing a specific mission.
     * @param mission The mission that was completed.
     * @param superiorPlayer The player to reward.
     * @param checkAutoReward Whether or not the auto reward flag should be checked.
     * @param forceReward Should the plugin force the reward to the player (no checks will be ran)
     */
    void rewardMission(Mission mission, SuperiorPlayer superiorPlayer, boolean checkAutoReward, boolean forceReward);

    /**
     * Save all data related to missions.
     * All the data is saved into a yaml file.
     */
    void saveMissionsData();

    /**
     * Load all data related to missions.
     * All the data is loaded from a yaml file.
     */
    void loadMissionsData();

}
