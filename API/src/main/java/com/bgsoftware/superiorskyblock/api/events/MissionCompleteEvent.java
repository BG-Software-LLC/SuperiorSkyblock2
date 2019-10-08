package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * MissionCompleteEvent is called when a player is completing a mission.
 */
public class MissionCompleteEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final Mission mission;
    private final boolean islandMission;

    private List<ItemStack> itemRewards;
    private List<String> commandRewards;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     * @param superiorPlayer The player who completed the mission.
     * @param mission The mission that was completed.
     * @param islandMission Flag that determines whether or not the mission is an island mission.
     * @param itemRewards The list of items that will be given as a reward.
     * @param commandRewards The list of commands that will be ran as a reward.
     */
    public MissionCompleteEvent(SuperiorPlayer superiorPlayer, Mission mission, boolean islandMission, List<ItemStack> itemRewards, List<String> commandRewards){
        this.superiorPlayer = superiorPlayer;
        this.mission = mission;
        this.islandMission = islandMission;
        this.itemRewards = itemRewards;
        this.commandRewards = commandRewards;
    }

    /**
     * Get the player who completed the mission.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the mission that was completed.
     */
    public Mission getMission() {
        return mission;
    }

    /**
     * Get the list of items that will be given as a reward.
     */
    public List<ItemStack> getItemRewards() {
        return itemRewards;
    }

    /**
     * Get the list of commands that will be given as a reward.
     */
    public List<String> getCommandRewards() {
        return commandRewards;
    }

    /**
     * Check whether or not the mission is an island mission.
     */
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
