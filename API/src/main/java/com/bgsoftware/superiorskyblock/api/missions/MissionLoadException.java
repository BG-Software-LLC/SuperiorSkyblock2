package com.bgsoftware.superiorskyblock.api.missions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This exception is used inside {@link Mission#load(JavaPlugin, ConfigurationSection)}
 * when a faulty configuration is used for the mission.
 */
public class MissionLoadException extends Exception {

    public MissionLoadException(String error) {
        super(error);
    }

}
