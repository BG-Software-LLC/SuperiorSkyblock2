package com.bgsoftware.superiorskyblock.nms.v1_20_2.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import org.bukkit.inventory.InventoryHolder;

public class MenuFurnaceBlockEntity extends FurnaceBlockEntity {

    private final InventoryHolder holder;

    public MenuFurnaceBlockEntity(InventoryHolder holder, String title) {
        super(BlockPos.ZERO, Blocks.AIR.defaultBlockState());
        this.holder = holder;
        this.setCustomName(Component.nullToEmpty(title));
    }

    @Override
    public InventoryHolder getOwner() {
        return holder;
    }

}
