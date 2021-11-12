package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;

public interface BanksFactory {

    /**
     * Create a new bank for an island.
     *
     * @param island   The island to create the bank for.
     * @param original The original island bank that was created.
     */
    IslandBank createIslandBank(Island island, IslandBank original);

}
