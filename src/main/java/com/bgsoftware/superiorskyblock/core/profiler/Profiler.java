package com.bgsoftware.superiorskyblock.core.profiler;

import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.stats.StatsProfilers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Profiler {

    private Profiler() {

    }

    private static final long INVALID_PROFILE_ID = -1;

    private static final Map<Long, ProfilerSession> profilerSessions = new ConcurrentHashMap<>();
    private static final AtomicLong lastProfilerId = new AtomicLong(0);

    public static long start(ProfileType profileType) {
        return start(profileType, 1);
    }

    public static long start(ProfileType profileType, int stopCount) {
        ProfilerSession profilerSession = new ProfilerSession(lastProfilerId.incrementAndGet(), stopCount, profileType);
        profilerSessions.put(profilerSession.getId(), profilerSession);
        return profilerSession.getId();
    }

    public static void end(long id) {
        if (id == INVALID_PROFILE_ID)
            return;

        ProfilerSession profilerSession = profilerSessions.get(id);

        if (profilerSession == null)
            return;

        if (profilerSession.end()) {
            profilerSessions.remove(id);
            String[] profiledData = profilerSession.dump();
            Log.profile(profiledData);
            StatsProfilers.INSTANCE.submitProfiler(profilerSession);
        }
    }

}
