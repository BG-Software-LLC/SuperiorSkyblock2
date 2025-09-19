package com.bgsoftware.superiorskyblock.nms.v1_16_R3.dragon;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.cache.IslandCache;
import com.bgsoftware.superiorskyblock.api.island.cache.IslandCacheKey;
import com.bgsoftware.superiorskyblock.island.cache.IslandCacheKeys;
import net.minecraft.server.v1_16_R3.EnderDragonBattle;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.WorldServer;

import java.util.LinkedList;
import java.util.List;

public class EndWorldEnderDragonBattleHandler extends EnderDragonBattle {

    private static final IslandCacheKey<IslandEnderDragonBattle> CACHE_KEY = IslandCacheKeys.register("DRAGON_BATTLE", IslandEnderDragonBattle.class);

    private final List<IslandEnderDragonBattle> worldDragonBattlesList = new LinkedList<>();

    public EndWorldEnderDragonBattleHandler(WorldServer worldServer) {
        super(worldServer, worldServer.getSeed(), new NBTTagCompound());
    }

    @Override
    public void b() {
        this.worldDragonBattlesList.forEach(EnderDragonBattle::b);
    }

    public void addDragonBattle(IslandCache islandCache, IslandEnderDragonBattle enderDragonBattle) {
        IslandEnderDragonBattle oldBattle = islandCache.store(CACHE_KEY, enderDragonBattle);
        if (oldBattle != null)
            this.worldDragonBattlesList.remove(oldBattle);
        this.worldDragonBattlesList.add(enderDragonBattle);
    }

    @Nullable
    public IslandEnderDragonBattle getDragonBattle(IslandCache islandCache) {
        return islandCache.get(CACHE_KEY);
    }

    @Nullable
    public IslandEnderDragonBattle removeDragonBattle(IslandCache islandCache) {
        IslandEnderDragonBattle enderDragonBattle = islandCache.remove(CACHE_KEY);
        if (enderDragonBattle != null)
            this.worldDragonBattlesList.remove(enderDragonBattle);
        return enderDragonBattle;
    }

}
