package com.bgsoftware.superiorskyblock.api.missions;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Mission {

    private String name = null;

    public void setName(String name){
        if(this.name == null)
            this.name = name;
    }

    public String getName(){
        return name;
    }

    public abstract void load(JavaPlugin plugin, ConfigurationSection missionSection) throws MissionLoadException;

    public abstract boolean canComplete(SuperiorPlayer superiorPlayer);

    public abstract void onComplete(SuperiorPlayer superiorPlayer);

    public abstract void onCompleteFail(SuperiorPlayer superiorPlayer);

}
