package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


/**
 * MissionResetEvent is called when a mission is reset.
 * After the event is executed, the holder may still have the mission completed if the times the holder completed
 * the mission was above 1.
 */
public class MissionResetEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final IMissionsHolder missionsHolder;
    private final Mission<?> mission;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that reset the mission.
     *                       If null, the mission was reset by console.
     * @param missionsHolder The holder of the mission that the mission was reset for.
     * @param mission        The mission that was reset.
     */
    public MissionResetEvent(@Nullable SuperiorPlayer superiorPlayer, IMissionsHolder missionsHolder, Mission<?> mission) {
        super(!Bukkit.isPrimaryThread());
        this.superiorPlayer = superiorPlayer;
        this.missionsHolder = missionsHolder;
        this.mission = mission;
    }

    /**
     * Get the player who reset the mission for the holder.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the holder of the mission that the mission was reset for.
     */
    public IMissionsHolder getMissionsHolder() {
        return missionsHolder;
    }

    /**
     * Get the mission that was reset.
     */
    public Mission<?> getMission() {
        return mission;
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
