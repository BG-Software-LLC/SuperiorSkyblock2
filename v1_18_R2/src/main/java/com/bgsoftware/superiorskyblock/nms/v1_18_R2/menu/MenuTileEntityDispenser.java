package com.bgsoftware.superiorskyblock.nms.v1_18_R2.menu;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block.Block;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.world.level.block.entity.TileEntityDispenser;
import net.minecraft.world.level.block.entity.TileEntityHopper;
import org.bukkit.inventory.InventoryHolder;

public final class MenuTileEntityDispenser extends TileEntityDispenser {

    private final InventoryHolder holder;

    public MenuTileEntityDispenser(InventoryHolder holder, String title) {
        super(BlockPosition.b, Block.AIR.getBlockData().getHandle());
        this.holder = holder;
        this.a(new ChatMessage(title));
    }

    @Override
    public InventoryHolder getOwner() {
        return holder;
    }

}
