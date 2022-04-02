package com.bgsoftware.superiorskyblock.nms.v1_18_R1.dragon;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEnderDragon;

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

    @Override
    public CraftEnderDragon getBukkitEntity() {
        return (CraftEnderDragon) super.getBukkitEntity();
    }

    public Entity getEntity() {
        return entity;
    }

}
