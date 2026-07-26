package com.bgsoftware.superiorskyblock.service.economy;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.hooks.PricesProvider;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.service.IService;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class EconomyService implements IService {

    private static final BigDecimal MAX_DOUBLE = BigDecimal.valueOf(Double.MAX_VALUE);

    private final SuperiorSkyblockPlugin plugin;

    private List<Runnable> pricesLoadCallbacks = new LinkedList<>();

    private EconomyProvider economyProvider;
    private EconomyProvider economyBankProvider;
    private PricesProvider pricesProvider;

    public EconomyService(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<?> getAPIClass() {
        return EconomyService.class;
    }

    public void setEconomyProvider(EconomyProvider economyProvider) {
        this.economyProvider = economyProvider;
    }

    public EconomyProvider getEconomyProvider() {
        return economyProvider;
    }

    public void setEconomyBankProvider(EconomyProvider economyBankProvider) {
        this.economyBankProvider = economyBankProvider;
    }

    public EconomyProvider getEconomyBankProvider() {
        return economyBankProvider;
    }

    public void setPricesProvider(PricesProvider pricesProvider) {
        this.pricesProvider = pricesProvider;
    }

    public PricesProvider getPricesProvider() {
        return pricesProvider;
    }

    public BigDecimal getBalance(SuperiorPlayer superiorPlayer) {
        return this.economyProvider.getBalance(superiorPlayer);
    }

    public BigDecimal getBankBalance(SuperiorPlayer superiorPlayer) {
        return this.economyProvider.getBalance(superiorPlayer);
    }

    public EconomyProvider.EconomyResult depositMoney(SuperiorPlayer superiorPlayer, BigDecimal amount) {
        return depositMoneyInternal(this.economyProvider, superiorPlayer, amount);
    }

    private static EconomyProvider.EconomyResult depositMoneyInternal(EconomyProvider economyProvider,
                                                                      SuperiorPlayer superiorPlayer,
                                                                      BigDecimal amount) {
        while (amount.compareTo(MAX_DOUBLE) > 0) {
            EconomyProvider.EconomyResult result = economyProvider.depositMoney(superiorPlayer, Double.MAX_VALUE);
            if (result.hasFailed())
                return result;

            amount = amount.subtract(MAX_DOUBLE);
        }

        return economyProvider.depositMoney(superiorPlayer, amount.doubleValue());
    }

    public EconomyProvider.EconomyResult depositBankMoney(SuperiorPlayer superiorPlayer, BigDecimal amount) {
        return withdrawMoneyInternal(this.economyBankProvider, superiorPlayer, amount);
    }

    public EconomyProvider.EconomyResult withdrawMoney(SuperiorPlayer superiorPlayer, BigDecimal amount) {
        return withdrawMoneyInternal(this.economyProvider, superiorPlayer, amount);
    }

    public EconomyProvider.EconomyResult withdrawBankMoney(SuperiorPlayer superiorPlayer, BigDecimal amount) {
        return withdrawMoneyInternal(this.economyBankProvider, superiorPlayer, amount);
    }

    private static EconomyProvider.EconomyResult withdrawMoneyInternal(EconomyProvider economyProvider,
                                                                       SuperiorPlayer superiorPlayer,
                                                                       BigDecimal amount) {
        while (amount.compareTo(MAX_DOUBLE) > 0) {
            EconomyProvider.EconomyResult result = economyProvider.withdrawMoney(superiorPlayer, Double.MAX_VALUE);
            if (result.hasFailed())
                return result;

            amount = amount.subtract(MAX_DOUBLE);
        }

        return economyProvider.withdrawMoney(superiorPlayer, amount.doubleValue());
    }

    public void forcePricesLoad() {
        if (this.pricesLoadCallbacks != null) {
            this.pricesLoadCallbacks.forEach(Runnable::run);
            this.pricesLoadCallbacks = null;
            // After we loaded all the price callbacks, we want to sort the top islands.
            SortingType.values().forEach(plugin.getGrid()::forceSortIslands);
        }
    }

    public void addPricesLoadCallback(Runnable callback) {
        if (this.pricesLoadCallbacks == null) {
            callback.run();
        } else {
            this.pricesLoadCallbacks.add(callback);
        }
    }

}
