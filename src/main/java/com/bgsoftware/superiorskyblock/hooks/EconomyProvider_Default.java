package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public final class EconomyProvider_Default implements EconomyProvider {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public double getMoneyInBank(SuperiorPlayer superiorPlayer) {
        return Double.MAX_VALUE;
    }

    @Override
    public String depositMoney(SuperiorPlayer superiorPlayer, double amount) {
        return "";
    }

    @Override
    public String withdrawMoney(SuperiorPlayer superiorPlayer, double amount) {
        return "";
    }

}
