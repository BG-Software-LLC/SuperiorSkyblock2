package com.bgsoftware.superiorskyblock.nms.v1_20_4.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.bukkit.inventory.InventoryHolder;

public class MenuHopperBlockEntity extends HopperBlockEntity {

    private final InventoryHolder holder;

    public MenuHopperBlockEntity(InventoryHolder holder, String title) {
        super(BlockPos.ZERO, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        this.holder = holder;
        this.name = Component.nullToEmpty(title);
    }

    @Override
    public InventoryHolder getOwner() {
        return holder;
    }

}
