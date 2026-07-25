package com.bgsoftware.superiorskyblock.nms.v26_2.dragon;

import com.bgsoftware.superiorskyblock.nms.v26_2.dragon.EndWorldEndDragonFightHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.end.EnderDragonFight;

import java.util.List;
import java.util.Optional;

public class EndDragonFightWrapper extends EnderDragonFight {

    public final EndWorldEndDragonFightHandler HANDLER = new EndWorldEndDragonFightHandler();

    public EndDragonFightWrapper(ServerLevel serverLevel, BlockPos islandPos) {
        super(true, false, false, Optional.empty(), 0, Optional.empty(), Optional.empty(), new ObjectArrayList(), List.of());
        init(serverLevel, serverLevel.getSeed(), islandPos);
    }

    @Override
    public void tick() {
        HANDLER.tick();
    }

    protected BlockPos getPortalPos() {
        return this.exitPortalLocation;
    }

    protected void setPortalPos(BlockPos blockPos) {
        this.exitPortalLocation = blockPos;
    }


}
