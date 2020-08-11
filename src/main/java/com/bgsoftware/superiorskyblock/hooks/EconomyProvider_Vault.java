package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import net.milkbowl.vault.economy.Economy;
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
    public void depositMoney(SuperiorPlayer superiorPlayer, double amount) {
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();
        econ.depositPlayer(offlinePlayer, amount);
    }

    @Override
    public void withdrawMoney(SuperiorPlayer superiorPlayer, double amount) {
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();
        econ.withdrawPlayer(offlinePlayer, amount);
    }

}
