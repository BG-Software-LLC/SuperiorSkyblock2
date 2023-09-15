package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.List;
import java.util.function.Consumer;

public interface MissionsManager {

    /**
     * Get a mission by its name.
     *
     * @param name The name to check.
     */
    @Nullable
    Mission<?> getMission(String name);

    /**
     * Get a list of all the missions.
     */
    List<Mission<?>> getAllMissions();

    /**
     * Get a list of all missions that are player missions.
     */
    List<Mission<?>> getPlayerMissions();

    /**
     * Get a list of all missions that are island missions.
     */
    List<Mission<?>> getIslandMissions();

    /**
     * Get a mission category by its name.
     *
     * @param name The name to check.
     */
    @Nullable
    MissionCategory getMissionCategory(String name);

    /**
     * Get all the mission categories.
     */
    List<MissionCategory> getMissionCategories();

    /**
     * Check whether or not the player has already completed the mission.
     *
     * @param superiorPlayer The player to check.
     * @param mission        The mission to check.
     * @return True if player has completed, otherwise false.
     */
    boolean hasCompleted(SuperiorPlayer superiorPlayer, Mission<?> mission);

    /**
     * Check whether or not a player can complete a mission.
     *
     * @param superiorPlayer The player to check.
     * @param mission        The mission to check.
     * @return True if player can complete, otherwise false.
     */
    boolean canComplete(SuperiorPlayer superiorPlayer, Mission<?> mission);

    /**
     * Check whether or not a player can complete a mission, without considering progress.
     *
     * @param superiorPlayer The player to check.
     * @param mission        The mission to check.
     * @return True if player can complete, otherwise false.
     */
    boolean canCompleteNoProgress(SuperiorPlayer superiorPlayer, Mission<?> mission);

    /**
     * Check whether or not the player can complete the mission again.
     *
     * @param superiorPlayer The player to check.
     * @param mission        The mission to check.
     * @return True if player can complete, otherwise false.
     */
    boolean canCompleteAgain(SuperiorPlayer superiorPlayer, Mission<?> mission);

    /**
     * Check whether or not a player has all the required missions to complete a mission.
     *
     * @param superiorPlayer The player to check.
     * @param mission        The mission to check.
     * @return True if player has all required missions, otherwise false.
     */
    boolean hasAllRequiredMissions(SuperiorPlayer superiorPlayer, Mission<?> mission);

    /**
     * Check whether or not a player can pass all the checks to complete a mission.
     *
     * @param superiorPlayer The player to check.
     * @param mission        The mission to check.
     * @return True if player can pass all checks, otherwise false.
     */
    boolean canPassAllChecks(SuperiorPlayer superiorPlayer, Mission<?> mission);

    /**
     * Reward a player for completing a specific mission.
     *
     * @param mission         The mission that was completed.
     * @param superiorPlayer  The player to reward.
     * @param checkAutoReward Whether or not the auto reward flag should be checked.
     */
    void rewardMission(Mission<?> mission, SuperiorPlayer superiorPlayer, boolean checkAutoReward);

    /**
     * Reward a player for completing a specific mission.
     *
     * @param mission         The mission that was completed.
     * @param superiorPlayer  The player to reward.
     * @param checkAutoReward Whether or not the auto reward flag should be checked.
     * @param forceReward     Should the plugin force the reward to the player (no checks will be ran)
     */
    void rewardMission(Mission<?> mission, SuperiorPlayer superiorPlayer, boolean checkAutoReward, boolean forceReward);

    /**
     * Reward a player for completing a specific mission.
     *
     * @param mission         The mission that was completed.
     * @param superiorPlayer  The player to reward.
     * @param checkAutoReward Whether or not the auto reward flag should be checked.
     * @param forceReward     Should the plugin force the reward to the player (no checks will be ran)
     * @param result          The result of the reward.
     */
    void rewardMission(Mission<?> mission, SuperiorPlayer superiorPlayer, boolean checkAutoReward, boolean forceReward, @Nullable Consumer<Boolean> result);

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

    /**
     * Load all data related to missions of specific missions.
     * All the data is loaded from a yaml file.
     */
    void loadMissionsData(List<Mission<?>> missionsList);

}
