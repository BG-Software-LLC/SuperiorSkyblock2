package com.bgsoftware.superiorskyblock.core.profiler;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfilerSession {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final long id;
    private final AtomicInteger stopCount;
    private final ProfileType profileType;
    @Nullable
    private final Object extra;
    private final Data startData;
    private Data endData;

    public ProfilerSession(long id, int stopCount, ProfileType profileType, @Nullable Object extra) {
        this.id = id;
        this.stopCount = new AtomicInteger(stopCount);
        this.profileType = profileType;
        this.extra = extra;
        this.startData = new Data();
    }

    public long getId() {
        return id;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    @Nullable
    public Object getExtra() {
        return extra;
    }

    public Data getStartData() {
        return startData;
    }

    public Data getEndData() {
        return Objects.requireNonNull(endData);
    }

    public boolean end() {
        boolean ended = this.stopCount.decrementAndGet() <= 0;
        if (ended)
            this.endData = new Data();
        return ended;
    }

    public String[] dump() {
        List<String> dump = new ArrayList<>();

        dump.add("Profiler #" + this.id);
        dump.add("  Type: " + this.profileType.getPrettyName());
        dump.add("  Time elapsed: " + TimeUnit.NANOSECONDS.toMillis(this.endData.time - this.startData.time) + "ms");
        dump.add("  TPS: " + this.startData.tps + " -> " + this.endData.tps);

        return dump.toArray(new String[0]);
    }

    public static class Data {


        public final long time = System.nanoTime();
        public final double tps = plugin.getNMSAlgorithms().getCurrentTps();

    }

}
