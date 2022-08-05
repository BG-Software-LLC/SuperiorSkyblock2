package com.bgsoftware.superiorskyblock.api.missions;

import java.util.List;
import java.util.Map;

public interface IMissionsHolder {

    /**
     * Complete a mission.
     *
     * @param mission The mission to complete.
     */
    void completeMission(Mission<?> mission);

    /**
     * Reset a mission.
     *
     * @param mission The mission to reset.
     */
    void resetMission(Mission<?> mission);

    /**
     * Check whether the island has completed the mission before.
     *
     * @param mission The mission to check.
     */
    boolean hasCompletedMission(Mission<?> mission);

    /**
     * Check whether the island can complete a mission again.
     *
     * @param mission The mission to check.
     */
    boolean canCompleteMissionAgain(Mission<?> mission);

    /**
     * Get the amount of times mission was completed.
     *
     * @param mission The mission to check.
     */
    int getAmountMissionCompleted(Mission<?> mission);

    /**
     * Set the amount of times mission was completed.
     *
     * @param mission     The mission to set.
     * @param finishCount The amount of times the mission was completed.
     */
    void setAmountMissionCompleted(Mission<?> mission, int finishCount);

    /**
     * Get the list of the completed missions of the player.
     */
    List<Mission<?>> getCompletedMissions();

    /**
     * Get all the completed missions with the amount of times they were completed.
     */
    Map<Mission<?>, Integer> getCompletedMissionsWithAmounts();

}
