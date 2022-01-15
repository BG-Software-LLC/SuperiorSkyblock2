package com.bgsoftware.superiorskyblock.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public class BlockStackEvent extends BlockEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final int originalCount;
    private final int newCount;
    private boolean cancelled = false;

    public BlockStackEvent(Block block, Player player, int originalCount, int newCount) {
        super(block);
        this.player = player;
        this.originalCount = originalCount;
        this.newCount = newCount;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public int getOriginalCount() {
        return originalCount;
    }

    public int getNewCount() {
        return newCount;
    }

    public int getIncreaseAmount() {
        return newCount - originalCount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
