package com.bgsoftware.superiorskyblock.missions.common;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.missions.MissionLoadException;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.List;

public abstract class BuiltinMission<V> extends Mission<V> {

    private final List<Listener> registeredListeners = new LinkedList<>();

    protected SuperiorSkyblock plugin;

    @Override
    public final void load(JavaPlugin plugin, ConfigurationSection section) throws MissionLoadException {
        this.plugin = (SuperiorSkyblock) plugin;

        loadConfiguration(section);
        registerListeners();

        setClearMethod(this::clearModuleData);
    }

    protected abstract void loadConfiguration(ConfigurationSection section) throws MissionLoadException;

    protected abstract void registerListeners();

    protected abstract void clearModuleData(V data);

    protected final void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        this.registeredListeners.add(listener);
    }

    @Override
    public void onComplete(SuperiorPlayer superiorPlayer) {

    }

    @Override
    public void onCompleteFail(SuperiorPlayer superiorPlayer) {

    }

    public final void unload() {
        this.registeredListeners.forEach(HandlerList::unregisterAll);
        this.registeredListeners.clear();
    }

}
