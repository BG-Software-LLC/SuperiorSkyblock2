package com.bgsoftware.superiorskyblock.nms.v1_18_R1.menu;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.Block;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.world.level.block.entity.TileEntityFurnaceFurnace;
import org.bukkit.inventory.InventoryHolder;

public class MenuTileEntityFurnace extends TileEntityFurnaceFurnace {

    private final InventoryHolder holder;

    public MenuTileEntityFurnace(InventoryHolder holder, String title) {
        super(BlockPosition.b, Block.AIR.getBlockData().getHandle());
        this.holder = holder;
        this.a(new ChatMessage(title));
    }

    @Override
    public InventoryHolder getOwner() {
        return holder;
    }

}
