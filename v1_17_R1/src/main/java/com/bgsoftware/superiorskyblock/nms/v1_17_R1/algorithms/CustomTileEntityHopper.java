package com.bgsoftware.superiorskyblock.nms.v1_17_R1.algorithms;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntityHopper;
import org.bukkit.inventory.InventoryHolder;

public final class CustomTileEntityHopper extends TileEntityHopper {

    private final InventoryHolder holder;

    public CustomTileEntityHopper(InventoryHolder holder, String title) {
        super(BlockPosition.b, Blocks.a.getBlockData());
        this.holder = holder;
        this.setCustomName(new ChatMessage(title));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public InventoryHolder getOwner() {
        return holder;
    }

}
