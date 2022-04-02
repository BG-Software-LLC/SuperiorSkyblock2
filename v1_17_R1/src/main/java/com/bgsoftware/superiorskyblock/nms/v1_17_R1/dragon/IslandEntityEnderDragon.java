package com.bgsoftware.superiorskyblock.nms.v1_17_R1.dragon;

import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEnderDragon;

public final class IslandEntityEnderDragon extends EntityEnderDragon {

    private final BlockPosition islandBlockPosition;

    IslandEntityEnderDragon(WorldServer worldServer, BlockPosition islandBlockPosition) {
        super(null, worldServer);
        this.islandBlockPosition = islandBlockPosition;
    }

    @Override
    public void movementTick() {
        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, super::movementTick);
    }

    @Override
    public CraftEnderDragon getBukkitEntity() {
        return (CraftEnderDragon) super.getBukkitEntity();
    }

}
