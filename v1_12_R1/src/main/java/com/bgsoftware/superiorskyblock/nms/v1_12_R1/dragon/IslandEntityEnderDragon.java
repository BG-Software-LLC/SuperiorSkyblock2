package com.bgsoftware.superiorskyblock.nms.v1_12_R1.dragon;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EntityEnderDragon;
import net.minecraft.server.v1_12_R1.WorldServer;

public final class IslandEntityEnderDragon extends EntityEnderDragon {

    private final BlockPosition islandBlockPosition;

    IslandEntityEnderDragon(WorldServer worldServer, BlockPosition islandBlockPosition) {
        super(worldServer);
        this.islandBlockPosition = islandBlockPosition;
    }

    @Override
    public void n() {
        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, super::n);
    }

}
