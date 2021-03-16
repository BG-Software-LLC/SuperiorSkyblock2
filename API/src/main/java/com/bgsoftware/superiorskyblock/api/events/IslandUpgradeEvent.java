package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

/**
 * IslandCreateEvent is called when a new island is created.
 */
public class IslandUpgradeEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final String upgradeName;
    private final List<String> commands;
    private UpgradeCost upgradeCost;
    private boolean cancelled = false;

    /**
     * The constructor for the event.
     * @param superiorPlayer The player who upgraded the island. Can be null if ran by the console.
     * @param island The island object that was upgraded.
     * @param upgradeName The name of the upgrade.
     * @param commands The commands that will be ran upon upgrade.
     * @param upgradeCost The cost of the upgrade
     */
    public IslandUpgradeEvent(SuperiorPlayer superiorPlayer, Island island, String upgradeName, List<String> commands, UpgradeCost upgradeCost){
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.upgradeName = upgradeName;
        this.commands = commands;
        this.upgradeCost = upgradeCost;
    }

    /**
     * Get the player who upgraded the island.
     * Can be null if ran by the console.
     */
    @Nullable
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
     * Get the upgrade cost that is used.
     */
    public UpgradeCost getUpgradeCost() {
        return upgradeCost;
    }

    /**
     * Set a new upgrade cost to be used.
     * @param upgradeCost The new upgrade cost.
     */
    public void setUpgradeCost(UpgradeCost upgradeCost){
        this.upgradeCost = upgradeCost;
    }

    /**
     * Get the amount that will be withdrawn.
     * @deprecated See getCost()
     */
    @Deprecated
    public double getAmountToWithdraw() {
        return getCost().doubleValue();
    }

    /**
     * Get the amount that will be withdrawn.
     */
    public BigDecimal getCost() {
        return upgradeCost.getCost();
    }

    /**
     * Set the amount that will be withdrawn.
     * @deprecated See setCost(BigDecimal)
     */
    @Deprecated
    public void setAmountToWithdraw(double amountToWithdraw) {
        setCost(BigDecimal.valueOf(amountToWithdraw));
    }

    /**
     * Set the amount that will be withdrawn.
     * @param cost The new amount to be withdrawn.
     */
    public void setCost(BigDecimal cost){
        setUpgradeCost(this.upgradeCost.clone(cost));
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
