package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyProvider_Vault implements EconomyProvider {

    private final Economy econ;

    public EconomyProvider_Vault() throws RuntimeException{
        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            throw new RuntimeException();

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);

        if (rsp == null)
            throw new RuntimeException();

        econ = rsp.getProvider();
    }

    @Override
    public double getMoneyInBank(SuperiorPlayer superiorPlayer) {
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();

        if(!econ.hasAccount(offlinePlayer))
            econ.createPlayerAccount(offlinePlayer);

        return econ.getBalance(offlinePlayer);
    }

    @Override
    public String depositMoney(SuperiorPlayer superiorPlayer, double amount) {
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();
        double currentMoney = getMoneyInBank(superiorPlayer);
        EconomyResponse economyResponse = econ.depositPlayer(offlinePlayer, amount);
        return getMoneyInBank(superiorPlayer) == currentMoney ? "You have exceed the limit of your bank" : economyResponse.errorMessage;
    }

    @Override
    public String withdrawMoney(SuperiorPlayer superiorPlayer, double amount) {
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();
        double currentMoney = getMoneyInBank(superiorPlayer);
        EconomyResponse economyResponse = econ.withdrawPlayer(offlinePlayer, amount);
        return getMoneyInBank(superiorPlayer) == currentMoney ? "Couldn't process the transaction" : economyResponse.errorMessage;
    }

}
