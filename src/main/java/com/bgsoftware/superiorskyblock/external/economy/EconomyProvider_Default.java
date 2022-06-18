package com.bgsoftware.superiorskyblock.external.economy;

import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.math.BigDecimal;

public class EconomyProvider_Default implements EconomyProvider {

    private static final BigDecimal MAX_DOUBLE = BigDecimal.valueOf(Double.MAX_VALUE);

    @Override
    public BigDecimal getBalance(SuperiorPlayer superiorPlayer) {
        return MAX_DOUBLE;
    }

    @Override
    public EconomyResult depositMoney(SuperiorPlayer superiorPlayer, double amount) {
        return new EconomyResult("&cServer doesn't have vault installed so transactions are disabled.");
    }

    @Override
    public EconomyResult withdrawMoney(SuperiorPlayer superiorPlayer, double amount) {
        return new EconomyResult("&cServer doesn't have vault installed so transactions are disabled.");
    }

}
