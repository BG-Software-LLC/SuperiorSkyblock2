package com.bgsoftware.superiorskyblock.missions.island.timings;

import org.spigotmc.CustomTimingsHandler;

public class LegacyTimingsWrapper implements ITimings {

    private final CustomTimingsHandler handle;

    public static ITimings create(String name) {
        return new LegacyTimingsWrapper(name);
    }

    private LegacyTimingsWrapper(String name) {
        this.handle = new CustomTimingsHandler(name);
    }

    @Override
    public void startTiming() {
        this.handle.startTiming();
    }

    @Override
    public void stopTiming() {
        this.handle.stopTiming();
    }

}
