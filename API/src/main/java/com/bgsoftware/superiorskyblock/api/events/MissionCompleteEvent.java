package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
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
    private final IMissionsHolder missionsHolder;
    private final Mission<?> mission;

    private final List<ItemStack> itemRewards;
    private final List<String> commandRewards;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player who completed the mission.
     * @param mission        The mission that was completed.
     * @param islandMission  Flag that determines whether or not the mission is an island mission.
     * @param itemRewards    The list of items that will be given as a reward.
     * @param commandRewards The list of commands that will be ran as a reward.
     */
    @Deprecated
    public MissionCompleteEvent(SuperiorPlayer superiorPlayer, Mission<?> mission,
                                boolean islandMission, List<ItemStack> itemRewards, List<String> commandRewards) {
        this(superiorPlayer, islandMission ? superiorPlayer.getIsland() : superiorPlayer, mission,
                itemRewards, commandRewards);
    }

    public MissionCompleteEvent(SuperiorPlayer superiorPlayer, IMissionsHolder missionsHolder,
                                Mission<?> mission, List<ItemStack> itemRewards, List<String> commandRewards) {
        super(!Bukkit.isPrimaryThread());
        this.superiorPlayer = superiorPlayer;
        this.missionsHolder = missionsHolder;
        this.mission = mission;
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
     * Get the mission holder that the mission was completed for.
     */
    public IMissionsHolder getMissionsHolder() {
        return missionsHolder;
    }

    /**
     * Get the mission that was completed.
     */
    public Mission<?> getMission() {
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
     * Check whether the mission is an island mission.
     */
    public boolean isIslandMission() {
        return mission.getIslandMission();
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
