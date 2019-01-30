package com.bgsoftware.superiorskyblock.listeners.events;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@SuppressWarnings("unused")
public final class ItemFrameBreakEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private ItemFrame itemFrame;

    public ItemFrameBreakEvent(Player who, ItemFrame itemFrame){
        super(who);
        this.itemFrame = itemFrame;
        this.cancelled = false;
    }

    public ItemFrame getItemFrame() {
        return itemFrame;
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

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
