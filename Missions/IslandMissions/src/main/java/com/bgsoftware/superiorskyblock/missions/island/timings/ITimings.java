package com.bgsoftware.superiorskyblock.missions.island.timings;

import org.bukkit.plugin.Plugin;

public interface ITimings {

    void startTiming();

    void stopTiming();

    static ITimings of(Plugin plugin, String name) {
        try {
            return TimingsWrapper.create(plugin, name);
        } catch (Throwable error) {
            try {
                return LegacyTimingsWrapper.create(name);
            } catch (Throwable error2) {
                return DummyTimings.INSTANCE;
            }
        }
    }

}
