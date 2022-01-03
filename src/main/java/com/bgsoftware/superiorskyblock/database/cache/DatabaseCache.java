package com.bgsoftware.superiorskyblock.database.cache;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DatabaseCache {

    private final Map<UUID, CachedIslandInfo> islandInfoMap = new HashMap<>();

    public DatabaseCache() {

    }

    public CachedIslandInfo addCachedIslandInfo(UUID uuid) {
        CachedIslandInfo cachedIslandInfo = new CachedIslandInfo();
        islandInfoMap.put(uuid, cachedIslandInfo);
        return cachedIslandInfo;
    }

    @Nullable
    public CachedIslandInfo getCachedIslandInfo(UUID uuid) {
        return islandInfoMap.get(uuid);
    }

    public void clearCache() {
        islandInfoMap.clear();
    }

}
