package com.bgsoftware.superiorskyblock.nms.v1_19_R1.chunks;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;

public record CropsTickingTileEntityTicker(
        CropsTickingTileEntity cropsTickingTileEntity) implements TickingBlockEntity {

    @Remap(classPath = "net.minecraft.world.level.block.entity.TickingBlockEntity",
            name = "tick",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Override
    public void a() {
        cropsTickingTileEntity.tick();
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.TickingBlockEntity",
            name = "isRemoved",
            type = Remap.Type.METHOD,
            remappedName = "b")
    @Override
    public boolean b() {
        return cropsTickingTileEntity.isRemoved();
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.TickingBlockEntity",
            name = "getPos",
            type = Remap.Type.METHOD,
            remappedName = "c")
    @Override
    public net.minecraft.core.BlockPosition c() {
        return cropsTickingTileEntity.getPosition();
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.TickingBlockEntity",
            name = "getType",
            type = Remap.Type.METHOD,
            remappedName = "d")
    @Override
    public String d() {
        return TileEntityTypes.a(cropsTickingTileEntity.getTileType()) + "";
    }

}
