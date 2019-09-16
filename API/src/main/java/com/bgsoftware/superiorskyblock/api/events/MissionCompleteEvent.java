package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MissionCompleteEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final Mission mission;
    private final boolean islandMission;

    private List<ItemStack> itemRewards;
    private List<String> commandRewards;
    private boolean cancelled = false;

    public MissionCompleteEvent(SuperiorPlayer superiorPlayer, Mission mission, boolean islandMission, List<ItemStack> itemRewards, List<String> commandRewards){
        this.superiorPlayer = superiorPlayer;
        this.mission = mission;
        this.islandMission = islandMission;
        this.itemRewards = itemRewards;
        this.commandRewards = commandRewards;
    }

    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    public Mission getMission() {
        return mission;
    }

    public List<ItemStack> getItemRewards() {
        return itemRewards;
    }

    public List<String> getCommandRewards() {
        return commandRewards;
    }

    public boolean isIslandMission() {
        return islandMission;
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
