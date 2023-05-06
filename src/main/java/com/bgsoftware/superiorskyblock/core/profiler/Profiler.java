package com.bgsoftware.superiorskyblock.core.profiler;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Profiler {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private Profiler() {

    }

    private static final long INVALID_PROFILE_ID = -1;

    private static final Map<Long, ProfilerSession> profilerSessions = new ConcurrentHashMap<>();
    private static final AtomicLong lastProfilerId = new AtomicLong(0);

    public static long start(ProfileType profileType) {
        return start(profileType, 1);
    }

    public static long start(ProfileType profileType, int stopCount) {
        if (!Log.isDebugged(Debug.PROFILER))
            return INVALID_PROFILE_ID;

        ProfilerSession profilerSession = new ProfilerSession(lastProfilerId.incrementAndGet(), stopCount, profileType);
        profilerSessions.put(profilerSession.id, profilerSession);
        return profilerSession.id;
    }

    public static void end(long id) {
        if (id == INVALID_PROFILE_ID)
            return;

        ProfilerSession profilerSession = profilerSessions.get(id);

        if (profilerSession == null)
            return;

        if (profilerSession.end()) {
            profilerSessions.remove(id);
            Log.profile(profilerSession.dump());
        }
    }

    private static class ProfilerSession {

        private final long id;
        private final AtomicInteger stopCount;
        private final ProfileType profileType;
        private final ProfiledData startData;
        private ProfiledData endData;

        ProfilerSession(long id, int stopCount, ProfileType profileType) {
            this.id = id;
            this.stopCount = new AtomicInteger(stopCount);
            this.profileType = profileType;
            this.startData = new ProfiledData();
        }

        boolean end() {
            boolean ended = this.stopCount.decrementAndGet() <= 0;
            if (ended)
                this.endData = new ProfiledData();
            return ended;
        }

        String[] dump() {
            List<String> dump = new ArrayList<>();

            dump.add("Profiler #" + this.id);
            dump.add("  Type: " + this.profileType.getPrettyName());
            dump.add("  Time elapsed: " + TimeUnit.NANOSECONDS.toMillis(this.endData.time - this.startData.time) + "ms");
            dump.add("  TPS: " + this.startData.tps + " -> " + this.endData.tps);

            return dump.toArray(new String[0]);
        }

    }

    private static class ProfiledData {

        private final long time = System.nanoTime();
        private final double tps = plugin.getNMSAlgorithms().getCurrentTps();

    }

}
