package com.bgsoftware.superiorskyblock.nms.v1_20_3.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.bukkit.inventory.InventoryHolder;

public class MenuBrewingStandBlockEntity extends BrewingStandBlockEntity {

    private final InventoryHolder holder;

    public MenuBrewingStandBlockEntity(InventoryHolder holder, String title) {
        super(BlockPos.ZERO, Blocks.AIR.defaultBlockState());
        this.holder = holder;
        this.setCustomName(Component.nullToEmpty(title));
    }

    @Override
    public InventoryHolder getOwner() {
        return holder;
    }

}
