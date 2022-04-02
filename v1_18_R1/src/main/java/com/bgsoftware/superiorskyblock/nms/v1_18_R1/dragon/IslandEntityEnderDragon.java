package com.bgsoftware.superiorskyblock.nms.v1_18_R1.dragon;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;

public final class IslandEntityEnderDragon extends EntityEnderDragon {

    private final BlockPosition islandBlockPosition;
    private final Entity entity = new Entity(this);

    IslandEntityEnderDragon(WorldServer worldServer, BlockPosition islandBlockPosition) {
        super(null, worldServer.getHandle());
        this.islandBlockPosition = islandBlockPosition;
    }

    @Override
    public void w_() {
        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, super::w_);
    }

    public Entity getEntity() {
        return entity;
    }

}
