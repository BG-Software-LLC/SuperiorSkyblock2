package com.bgsoftware.superiorskyblock.nms.v1_20_4.dragon;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.cache.IslandCache;
import com.bgsoftware.superiorskyblock.api.island.cache.IslandCacheKey;
import com.bgsoftware.superiorskyblock.island.cache.IslandCacheKeys;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.end.EndDragonFight;

import java.util.LinkedList;
import java.util.List;

public class EndWorldEndDragonFightHandler extends EndDragonFight {

    private static final IslandCacheKey<IslandEndDragonFight> CACHE_KEY = IslandCacheKeys.register("DRAGON_FIGHT", IslandEndDragonFight.class);

    private final List<IslandEndDragonFight> worldDragonFightsList = new LinkedList<>();

    public EndWorldEndDragonFightHandler(ServerLevel serverLevel) {
        super(serverLevel, serverLevel.getSeed(), serverLevel.serverLevelData.endDragonFightData());
    }

    @Override
    public void tick() {
        this.worldDragonFightsList.forEach(EndDragonFight::tick);
    }

    public void addDragonFight(IslandCache islandCache, IslandEndDragonFight endDragonFight) {
        IslandEndDragonFight oldDragonFight = islandCache.store(CACHE_KEY, endDragonFight);
        if (oldDragonFight != null)
            this.worldDragonFightsList.remove(oldDragonFight);
        this.worldDragonFightsList.add(endDragonFight);
    }

    @Nullable
    public IslandEndDragonFight getDragonFight(IslandCache islandCache) {
        return islandCache.get(CACHE_KEY);
    }

    @Nullable
    public IslandEndDragonFight removeDragonFight(IslandCache islandCache) {
        IslandEndDragonFight endDragonFight = islandCache.remove(CACHE_KEY);
        if (endDragonFight != null)
            this.worldDragonFightsList.remove(endDragonFight);
        return endDragonFight;
    }

}
