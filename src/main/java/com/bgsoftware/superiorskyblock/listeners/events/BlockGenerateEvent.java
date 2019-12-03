package com.bgsoftware.superiorskyblock.listeners.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class BlockGenerateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Block block;
    private final Island island;

    private Key newStateKey = Key.of("COBBLESTONE");

    public BlockGenerateEvent(Block block, Island island){
        super(true);
        this.block = block;
        this.island = island;
    }

    public Block getBlock() {
        return block;
    }

    public Island getIsland() {
        return island;
    }

    public Key getNewStateKey() {
        return newStateKey;
    }

    public void setNewStateKey(Key newStateKey) {
        this.newStateKey = newStateKey;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
