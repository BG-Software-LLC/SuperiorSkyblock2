package com.bgsoftware.superiorskyblock.listeners.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public class DragonEggChangeEvent extends BlockEvent {

    private static final HandlerList handlers = new HandlerList();

    public DragonEggChangeEvent(Block block){
        super(block);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
