package com.bgsoftware.superiorskyblock.nms.v1_21.dragon;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.nms.v1_21.dragon.EndWorldEndDragonFightHandler;
import com.bgsoftware.superiorskyblock.nms.v1_21.dragon.IslandEndDragonFight;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.EndDragonFight;

public class EndDragonFightWrapper extends EndDragonFight {

    public final EndWorldEndDragonFightHandler HANDLER = new EndWorldEndDragonFightHandler();

    public EndDragonFightWrapper(ServerLevel serverLevel, BlockPos islandPos) {
        super(serverLevel, serverLevel.getSeed(), serverLevel.serverLevelData.endDragonFightData(), islandPos);
        // Vanilla checks if the chunks around the origin are loaded before ticking the fight.
        // Island fights are only ticked when players are on the island, therefore we skip this check.
        skipArenaLoadedCheck();
    }

    @Override
    public void tick() {
        if (this instanceof IslandEndDragonFight) {
            // Island fights run the vanilla logic of the fight.
            super.tick();
            return;
        }

        HANDLER.tick();
    }

    @Override
    public boolean tryRespawn() {
        if (this instanceof IslandEndDragonFight)
            return super.tryRespawn();

        // The crystal was placed on an island, so we try to respawn the dragons of the islands instead.
        boolean respawned = false;
        for (IslandEndDragonFight endDragonFight : HANDLER.getDragonFights())
            respawned |= endDragonFight.tryRespawn();
        return respawned;
    }

    @Override
    public boolean tryRespawn(@Nullable BlockPos placedEndCrystalPos) {
        if (this instanceof IslandEndDragonFight)
            return super.tryRespawn(placedEndCrystalPos);

        if (placedEndCrystalPos == null)
            return tryRespawn();

        IslandEndDragonFight endDragonFight = HANDLER.getDragonFightAt(placedEndCrystalPos);
        return endDragonFight != null && endDragonFight.tryRespawn(placedEndCrystalPos);
    }

    @Override
    public void onCrystalDestroyed(EndCrystal endCrystal, DamageSource damageSource) {
        if (this instanceof IslandEndDragonFight) {
            super.onCrystalDestroyed(endCrystal, damageSource);
            return;
        }

        IslandEndDragonFight endDragonFight = HANDLER.getDragonFightAt(endCrystal.blockPosition());
        if (endDragonFight != null)
            endDragonFight.onCrystalDestroyed(endCrystal, damageSource);
    }

    protected BlockPos getPortalPos() {
        return this.portalLocation;
    }

    protected void setPortalPos(BlockPos blockPos) {
        this.portalLocation = blockPos;
    }



}
