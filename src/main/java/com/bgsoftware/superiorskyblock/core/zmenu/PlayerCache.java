package com.bgsoftware.superiorskyblock.core.zmenu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.UUID;

public class PlayerCache {

    private final Player player;
    private String islandName;
    private SuperiorPlayer targetPlayer;
    private SortingType sortingType;
    private Comparator<BankTransaction> bankSorting;
    private UUID filteredPlayer;
    private Island island;

    public PlayerCache(Player player) {
        this.player = player;
    }

    public String getIslandName() {
        return islandName;
    }

    public void setIslandName(String islandName) {
        this.islandName = islandName;
    }

    public Player getPlayer() {
        return player;
    }

    public SuperiorPlayer getTargetPlayer() {
        return targetPlayer;
    }

    public void setTargetPlayer(SuperiorPlayer targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    public SortingType getSortingType() {
        return this.sortingType == null ? SortingTypes.BY_WORTH : this.sortingType;
    }

    public void setSortingType(SortingType sortingType) {
        this.sortingType = sortingType;
    }

    public Comparator<BankTransaction> getBankSorting() {
        return bankSorting;
    }

    public void setBankSorting(Comparator<BankTransaction> bankSorting) {
        this.bankSorting = bankSorting;
    }

    public UUID getFilteredPlayer() {
        return filteredPlayer;
    }

    public void setFilteredPlayer(UUID filteredPlayer) {
        this.filteredPlayer = filteredPlayer;
    }

    public Island getIsland() {
        return island;
    }

    public void setIsland(Island island) {
        this.island = island;
    }
}
