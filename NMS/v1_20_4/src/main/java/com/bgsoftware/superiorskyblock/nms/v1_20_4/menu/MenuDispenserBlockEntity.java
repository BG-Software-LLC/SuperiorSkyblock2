package com.bgsoftware.superiorskyblock.nms.v1_20_4.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import org.bukkit.inventory.InventoryHolder;

public class MenuDispenserBlockEntity extends DispenserBlockEntity {

    private final InventoryHolder holder;

    public MenuDispenserBlockEntity(InventoryHolder holder, String title) {
        super(BlockPos.ZERO, Blocks.AIR.defaultBlockState());
        this.holder = holder;
        this.name = Component.nullToEmpty(title);
    }

    @Override
    public InventoryHolder getOwner() {
        return holder;
    }

}
