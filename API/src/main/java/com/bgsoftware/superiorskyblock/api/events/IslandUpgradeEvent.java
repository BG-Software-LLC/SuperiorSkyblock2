package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * IslandCreateEvent is called when a new island is created.
 */
public class IslandUpgradeEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final Upgrade upgrade;
    private final UpgradeLevel upgradeLevel;
    private final List<String> commands;
    private final Cause cause;
    @Nullable
    private UpgradeCost upgradeCost;
    private boolean cancelled = false;

    /**
     * The constructor for the event.
     *
     * @param superiorPlayer The player who upgraded the island. Can be null if ran by the console.
     * @param island         The island that was upgraded.
     * @param upgradeName    The name of the upgrade.
     * @param commands       The commands that will be ran upon upgrade.
     * @param upgradeCost    The cost of the upgrade
     * @deprecated See {@link #IslandUpgradeEvent(SuperiorPlayer, Island, Upgrade, UpgradeLevel, List, Cause, UpgradeCost)}
     */
    @Deprecated
    public IslandUpgradeEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, String upgradeName,
                              List<String> commands, @Nullable UpgradeCost upgradeCost) {
        super(island);
        Upgrade upgrade = SuperiorSkyblockAPI.getUpgrades().getUpgrade(upgradeName);

        this.superiorPlayer = superiorPlayer;
        this.upgrade = Preconditions.checkNotNull(upgrade, "upgrade cannot be null");
        this.upgradeLevel = Preconditions.checkNotNull(island.getUpgradeLevel(upgrade), "upgradeLevel cannot be null");
        this.commands = new LinkedList<>(Preconditions.checkNotNull(commands, "commands cannot be null"));
        this.cause = Cause.UNKONWN;
        this.upgradeCost = upgradeCost;
    }

    /**
     * The constructor for the event.
     *
     * @param superiorPlayer The player who upgraded the island.
     *                       Can be null if ran by the console.
     * @param island         The island that was upgraded.
     * @param upgrade        The upgrade.
     * @param upgradeLevel   The level that will be upgraded into.
     * @param commands       The commands that will be running upon upgrade.
     * @param upgradeCost    The cost of the upgrade.
     *                       If null, there was no cost for the upgrade (For example, setupgrade command).
     */
    @Deprecated
    public IslandUpgradeEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, Upgrade upgrade,
                              UpgradeLevel upgradeLevel, List<String> commands, @Nullable UpgradeCost upgradeCost) {
        this(superiorPlayer, island, upgrade, upgradeLevel, commands, Cause.UNKONWN, upgradeCost);
    }

    /**
     * The constructor for the event.
     *
     * @param superiorPlayer The player who upgraded the island.
     *                       Can be null if ran by the console.
     * @param island         The island that was upgraded.
     * @param upgrade        The upgrade.
     * @param upgradeLevel   The level that will be upgraded into.
     * @param commands       The commands that will be running upon upgrade.
     * @param upgradeCost    The cost of the upgrade.
     *                       If null, there was no cost for the upgrade (For example, setupgrade command).
     * @param cause          The cause of the upgrade.
     */
    public IslandUpgradeEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, Upgrade upgrade,
                              UpgradeLevel upgradeLevel, List<String> commands, Cause cause,
                              @Nullable UpgradeCost upgradeCost) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.upgrade = Preconditions.checkNotNull(upgrade, "upgrade cannot be null");
        this.upgradeLevel = Preconditions.checkNotNull(upgradeLevel, "upgradeLevel cannot be null");
        this.commands = new LinkedList<>(Preconditions.checkNotNull(commands, "commands cannot be null"));
        this.cause = Preconditions.checkNotNull(cause, "cause cannot be null");
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
        return upgrade.getName();
    }

    /**
     * Get the name of the upgrade.
     */
    public Upgrade getUpgrade() {
        return upgrade;
    }

    /**
     * Get the level that will be upgraded to.
     */
    public UpgradeLevel getUpgradeLevel() {
        return upgradeLevel;
    }

    /**
     * Get the commands that will be ran upon upgrade.
     */
    public List<String> getCommands() {
        return commands;
    }

    /**
     * Get the cause of this event.
     */
    public Cause getCause() {
        return cause;
    }

    /**
     * Get the upgrade cost that is used.
     */
    @Nullable
    public UpgradeCost getUpgradeCost() {
        return upgradeCost;
    }

    /**
     * Set a new upgrade cost to be used.
     *
     * @param upgradeCost The new upgrade cost.
     */
    public void setUpgradeCost(@Nullable UpgradeCost upgradeCost) {
        this.upgradeCost = upgradeCost;
    }

    /**
     * Get the amount that will be withdrawn.
     *
     * @deprecated See getCost()
     */
    @Deprecated
    public double getAmountToWithdraw() {
        return getCost().doubleValue();
    }

    /**
     * Set the amount that will be withdrawn.
     *
     * @deprecated See setCost(BigDecimal)
     */
    @Deprecated
    public void setAmountToWithdraw(double amountToWithdraw) {
        setCost(BigDecimal.valueOf(amountToWithdraw));
    }

    /**
     * Get the amount that will be withdrawn.
     */
    public BigDecimal getCost() {
        return upgradeCost == null ? BigDecimal.ZERO : upgradeCost.getCost();
    }

    /**
     * Set the amount that will be withdrawn.
     *
     * @param cost The new amount to be withdrawn.
     * @throws IllegalStateException If the upgradeCost is null. Use {@link #setUpgradeCost(UpgradeCost)} instead.
     */
    public void setCost(BigDecimal cost) throws IllegalStateException {
        if (this.upgradeCost == null)
            throw new IllegalStateException("Cannot set raw cost when upgradeCost is null.");

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


    public enum Cause {

        /**
         * Used when player runs '/is rankup'.
         */
        PLAYER_RANKUP,

        /**
         * Used when player or console runs '/is admin rankup'.
         */
        ADMIN_RANKUP,

        /**
         * Used when an admin or console runs '/is admin setupgrade'
         */
        ADMIN_SET_UPGRADE,

        /**
         * Used only for deprecated usage of the old constructors.
         */
        @Deprecated
        UNKONWN

    }

}
