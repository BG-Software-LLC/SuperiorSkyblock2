package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;

public final class EconomyProvider_Vault implements EconomyProvider {

    private static Economy econ;

    public static boolean isCompatible(){
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null)
            econ = rsp.getProvider();

        if(econ != null)
            SuperiorSkyblockPlugin.log("Using Vault as an economy provider.");

        return econ != null;
    }

    @Override
    public BigDecimal getBalance(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return BigDecimal.valueOf(getMoneyInBank(superiorPlayer.asOfflinePlayer()));
    }

    @Override
    public String depositMoney(SuperiorPlayer superiorPlayer, double amount) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();
        double currentMoney = getMoneyInBank(offlinePlayer);
        EconomyResponse economyResponse = econ.depositPlayer(offlinePlayer, amount);
        return getMoneyInBank(offlinePlayer) == currentMoney ? "You have exceed the limit of your bank" : economyResponse.errorMessage;
    }

    @Override
    public String withdrawMoney(SuperiorPlayer superiorPlayer, double amount) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();
        double currentMoney = getMoneyInBank(offlinePlayer);
        EconomyResponse economyResponse = econ.withdrawPlayer(offlinePlayer, amount);
        return getMoneyInBank(offlinePlayer) == currentMoney ? "Couldn't process the transaction" : economyResponse.errorMessage;
    }

    private double getMoneyInBank(OfflinePlayer offlinePlayer) {
        if(!econ.hasAccount(offlinePlayer))
            econ.createPlayerAccount(offlinePlayer);

        return econ.getBalance(offlinePlayer);
    }

}
