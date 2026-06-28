package com.bgsoftware.superiorskyblock.nms.v1_20_3.dragon;

import com.bgsoftware.superiorskyblock.nms.v1_20_3.dragon.EndWorldEndDragonFightHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.end.EndDragonFight;

public class EndDragonFightWrapper extends EndDragonFight {

    public final EndWorldEndDragonFightHandler HANDLER = new EndWorldEndDragonFightHandler();

    public EndDragonFightWrapper(ServerLevel serverLevel, BlockPos unused) {
        super(serverLevel, serverLevel.getSeed(), serverLevel.serverLevelData.endDragonFightData());
    }

    @Override
    public void tick() {
        HANDLER.tick();
    }

    protected BlockPos getPortalPos() {
        return this.portalLocation;
    }

    protected void setPortalPos(BlockPos blockPos) {
        this.portalLocation = blockPos;
    }



}
