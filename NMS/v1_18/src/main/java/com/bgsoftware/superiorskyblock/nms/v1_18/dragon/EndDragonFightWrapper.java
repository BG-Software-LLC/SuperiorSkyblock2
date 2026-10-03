package com.bgsoftware.superiorskyblock.nms.v1_18.dragon;

import com.bgsoftware.superiorskyblock.nms.v1_18.dragon.EndWorldEndDragonFightHandler;
import com.bgsoftware.superiorskyblock.nms.v1_18.dragon.IslandEndDragonFight;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.EndDragonFight;

public class EndDragonFightWrapper extends EndDragonFight {

    public final EndWorldEndDragonFightHandler HANDLER = new EndWorldEndDragonFightHandler();

    public EndDragonFightWrapper(ServerLevel serverLevel, BlockPos unused) {
        super(serverLevel, serverLevel.getSeed(), serverLevel.serverLevelData.endDragonFightData());
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
    public void tryRespawn() {
        if (this instanceof IslandEndDragonFight) {
            super.tryRespawn();
            return;
        }

        // The crystal was placed on an island, so we try to respawn the dragons of the islands instead.
        for (IslandEndDragonFight endDragonFight : HANDLER.getDragonFights())
            endDragonFight.tryRespawn();
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
