package com.bgsoftware.superiorskyblock.api.missions;

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

    public void setName(String name){
        if(this.name == null)
            this.name = name;
    }

    public String getName(){
        return name;
    }

    public void addRequiredMission(String... missions){
        requiredMissions.addAll(Arrays.asList(missions));
    }

    public List<String> getRequiredMissions(){
        return new ArrayList<>(requiredMissions);
    }


    public void toggleOnlyShowIfRequiredCompleted(){
        onlyShowIfRequiredCompleted = !onlyShowIfRequiredCompleted;
    }

    public boolean isOnlyShowIfRequiredCompleted(){
        return onlyShowIfRequiredCompleted;
    }

    public abstract void load(JavaPlugin plugin, ConfigurationSection missionSection) throws MissionLoadException;

    public abstract boolean canComplete(SuperiorPlayer superiorPlayer);

    public abstract void onComplete(SuperiorPlayer superiorPlayer);

    public abstract void onCompleteFail(SuperiorPlayer superiorPlayer);

}
