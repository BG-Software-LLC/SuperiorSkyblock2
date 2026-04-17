package com.bgsoftware.superiorskyblock.missions.island.timings;

public class DummyTimings implements ITimings {

    public static final DummyTimings INSTANCE = new DummyTimings();

    private DummyTimings() {

    }

    @Override
    public void startTiming() {
        // Do nothing.
    }

    @Override
    public void stopTiming() {
        // Do nothing.
    }

}
