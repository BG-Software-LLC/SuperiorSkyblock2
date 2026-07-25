package com.bgsoftware.superiorskyblock.core.factory;

import com.bgsoftware.superiorskyblock.api.factory.BanksFactory;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;

public class DefaultBanksFactory implements BanksFactory {

    private static final DefaultBanksFactory INSTANCE = new DefaultBanksFactory();

    public static DefaultBanksFactory getInstance() {
        return INSTANCE;
    }

    private DefaultBanksFactory() {
    }

    @Override
    public IslandBank createIslandBank(Island island, IslandBank original) {
        return original;
    }

}
