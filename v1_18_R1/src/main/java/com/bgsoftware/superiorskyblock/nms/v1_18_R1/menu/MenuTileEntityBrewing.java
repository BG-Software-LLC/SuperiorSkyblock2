package com.bgsoftware.superiorskyblock.nms.v1_18_R1.menu;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.Block;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.world.level.block.entity.TileEntityBrewingStand;
import org.bukkit.inventory.InventoryHolder;

public final class MenuTileEntityBrewing extends TileEntityBrewingStand {

    private final InventoryHolder holder;

    public MenuTileEntityBrewing(InventoryHolder holder, String title) {
        super(BlockPosition.b, Block.AIR.getBlockData().getHandle());
        this.holder = holder;
        this.a(new ChatMessage(title));
    }

    @Override
    public InventoryHolder getOwner() {
        return holder;
    }

}
