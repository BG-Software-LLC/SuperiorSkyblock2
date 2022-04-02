package com.bgsoftware.superiorskyblock.nms.v1_16_R3.dragon;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityEnderDragon;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEnderDragon;

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
