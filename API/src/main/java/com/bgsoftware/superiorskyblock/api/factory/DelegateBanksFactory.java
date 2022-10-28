package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;

public class DelegateBanksFactory implements BanksFactory {

    protected final BanksFactory handle;

    protected DelegateBanksFactory(BanksFactory handle) {
        this.handle = handle;
    }

    @Override
    public IslandBank createIslandBank(Island island, IslandBank original) {
        return this.handle.createIslandBank(island, original);
    }

}
