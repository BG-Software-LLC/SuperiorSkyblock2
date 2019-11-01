package com.bgsoftware.superiorskyblock.api.missions;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Mission {

    private String name = null;
    private List<String> requiredMissions = new ArrayList<>();
    private boolean onlyShowIfRequiredCompleted = false;

    /**
     * Set the name of the mission.
     * @param name The name to set.
     */
    public void setName(String name){
        if(this.name == null)
            this.name = name;
    }

    /**
     * Get the name of the mission.
     */
    public String getName(){
        return name;
    }

    /**
     * Add required missions for completing this mission.
     * @param missions The array of required missions.
     */
    public void addRequiredMission(String... missions){
        requiredMissions.addAll(Arrays.asList(missions));
    }

    /**
     * Get the required missions for completing this mission.
     */
    public List<String> getRequiredMissions(){
        return new ArrayList<>(requiredMissions);
    }

    /**
     * Toggle the onlyShowIfRequiredCompleted flag.
     */
    public void toggleOnlyShowIfRequiredCompleted(){
        onlyShowIfRequiredCompleted = !onlyShowIfRequiredCompleted;
    }

    /**
     * Check whether or not the item in the gui should be shown only
     * if all required missions are completed.
     */
    public boolean isOnlyShowIfRequiredCompleted(){
        return onlyShowIfRequiredCompleted;
    }

    /**
     * The load function of the mission.
     * @param plugin The plugin that loaded the mission (The SuperiorSkyblock's JavaPlugin class)
     * @param missionSection The configuration section of the mission from the config
     * @throws MissionLoadException if load was not success.
     */
    public abstract void load(JavaPlugin plugin, ConfigurationSection missionSection) throws MissionLoadException;

    /**
     * Get the progress of a specific player.
     * Method should return a value between 0.0 and 1.0
     * @param superiorPlayer The player to check.
     */
    public abstract double getProgress(SuperiorPlayer superiorPlayer);

    /**
     * Check whether or not a player can complete the mission.
     * @param superiorPlayer The player to check.
     */
    public boolean canComplete(SuperiorPlayer superiorPlayer){
        if(!superiorPlayer.getWorld().getName().equals(SuperiorSkyblockAPI.getIslandsWorld().getName()))
            return false;

        return getProgress(superiorPlayer) >= 1.0;
    }

    /**
     * A function that is called when a player is completing the mission.
     * @param superiorPlayer The player that completed the mission.
     */
    public abstract void onComplete(SuperiorPlayer superiorPlayer);

    /**
     * A function that is called when a player cannot complete the mission.
     * @param superiorPlayer The player that tried to complete the mission.
     */
    public abstract void onCompleteFail(SuperiorPlayer superiorPlayer);

}
