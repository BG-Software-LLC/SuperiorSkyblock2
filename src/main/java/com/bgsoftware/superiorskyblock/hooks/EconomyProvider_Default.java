package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.math.BigDecimal;

public final class EconomyProvider_Default implements EconomyProvider {

    private static final BigDecimal MAX_DOUBLE = BigDecimal.valueOf(Double.MAX_VALUE);

    @Override
    public BigDecimal getBalance(SuperiorPlayer superiorPlayer) {
        return MAX_DOUBLE;
    }

    @Override
    public String depositMoney(SuperiorPlayer superiorPlayer, double amount) {
        return "&cServer doesn't have vault installed so transactions are disabled.";
    }

    @Override
    public String withdrawMoney(SuperiorPlayer superiorPlayer, double amount) {
        return "&cServer doesn't have vault installed so transactions are disabled.";
    }

}
