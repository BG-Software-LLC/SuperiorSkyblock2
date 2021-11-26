package com.bgsoftware.superiorskyblock.nms.v1_18_R1.algorithms;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntityHopper;
import org.bukkit.inventory.InventoryHolder;

import static com.bgsoftware.superiorskyblock.nms.v1_18_R1.NMSMappings.*;

public final class CustomTileEntityHopper extends TileEntityHopper {

    private final InventoryHolder holder;

    public CustomTileEntityHopper(InventoryHolder holder, String title) {
        super(BlockPosition.b, getBlockData(Blocks.a));
        this.holder = holder;
        this.a(new ChatMessage(title));
    }

    @Override
    public InventoryHolder getOwner() {
        return holder;
    }

}
