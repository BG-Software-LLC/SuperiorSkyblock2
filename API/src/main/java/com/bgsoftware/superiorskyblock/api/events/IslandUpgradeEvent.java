package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

import java.util.List;

/**
 * IslandCreateEvent is called when a new island is created.
 */
public class IslandUpgradeEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final String upgradeName;
    private final List<String> commands;
    private double amountToWithdraw;
    private boolean cancelled = false;

    /**
     * The constructor for the event.
     * @param superiorPlayer The player who upgraded the island. Can be null if ran by the console.
     * @param island The island object that was upgraded.
     * @param upgradeName The name of the upgrade.
     * @param commands The commands that will be ran upon upgrade.
     * @param amountToWithdraw The amount of money that will be withdrawn.
     */
    public IslandUpgradeEvent(SuperiorPlayer superiorPlayer, Island island, String upgradeName, List<String> commands, double amountToWithdraw){
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.upgradeName = upgradeName;
        this.commands = commands;
        this.amountToWithdraw = amountToWithdraw;
    }

    /**
     * Get the player who upgraded the island.
     * Can be null if ran by the console.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the name of the upgrade.
     */
    public String getUpgradeName() {
        return upgradeName;
    }

    /**
     * Get the commands that will be ran upon upgrade.
     */
    public List<String> getCommands() {
        return commands;
    }

    /**
     * Get the amount that will be withdrawn.
     */
    public double getAmountToWithdraw() {
        return amountToWithdraw;
    }

    /**
     * Set the amount that will be withdrawn.
     */
    public void setAmountToWithdraw(double amountToWithdraw) {
        this.amountToWithdraw = amountToWithdraw;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
