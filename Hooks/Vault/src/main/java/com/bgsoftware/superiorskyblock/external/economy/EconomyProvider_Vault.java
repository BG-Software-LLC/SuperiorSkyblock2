package com.bgsoftware.superiorskyblock.external.economy;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Precision;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EconomyProvider_Vault implements EconomyProvider {

    private static Economy econ;

    public static boolean isCompatible() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null)
            econ = rsp.getProvider();

        if (econ != null)
            Log.info("Using Vault as an economy provider.");

        return econ != null;
    }

    @Override
    public BigDecimal getBalance(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return BigDecimal.valueOf(getMoneyInBank(superiorPlayer.asOfflinePlayer()))
                .setScale(3, RoundingMode.HALF_DOWN);
    }

    @Override
    public EconomyResult depositMoney(SuperiorPlayer superiorPlayer, double amount) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();
        double moneyBeforeDeposit = getMoneyInBank(offlinePlayer);
        EconomyResponse economyResponse = econ.depositPlayer(offlinePlayer, amount);
        double moneyInTransaction = Precision.round(getMoneyInBank(offlinePlayer) - moneyBeforeDeposit, 3);

        String errorMessage = moneyInTransaction == amount ? getErrorMessageFromResponse(economyResponse) :
                moneyInTransaction == 0 ? "You have exceed the limit of your bank" : "";

        return new EconomyResult(errorMessage, moneyInTransaction);
    }

    @Override
    public EconomyResult withdrawMoney(SuperiorPlayer superiorPlayer, double amount) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();
        double moneyBeforeWithdraw = getMoneyInBank(offlinePlayer);
        EconomyResponse economyResponse = econ.withdrawPlayer(offlinePlayer, amount);
        double moneyInTransaction = Precision.round(moneyBeforeWithdraw - getMoneyInBank(offlinePlayer), 3);

        String errorMessage = moneyInTransaction == amount ? getErrorMessageFromResponse(economyResponse) :
                moneyInTransaction == 0 ? "Couldn't process the transaction" : "";

        return new EconomyResult(errorMessage, moneyInTransaction);
    }

    private double getMoneyInBank(OfflinePlayer offlinePlayer) {
        if (!econ.hasAccount(offlinePlayer))
            econ.createPlayerAccount(offlinePlayer);

        return Precision.round(econ.getBalance(offlinePlayer), 3);
    }

    @Nullable
    private static String getErrorMessageFromResponse(EconomyResponse economyResponse) {
        return economyResponse.transactionSuccess() ? null : economyResponse.errorMessage;
    }

}
