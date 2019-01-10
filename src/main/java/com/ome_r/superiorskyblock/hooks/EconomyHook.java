package com.ome_r.superiorskyblock.hooks;

import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHook {

    private static Economy econ;

    static{
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            econ = null;
        }else {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                econ = null;
            }else {
                econ = rsp.getProvider();
            }
        }
    }

    public static double getMoneyInBank(WrappedPlayer wrappedPlayer){
        return getMoneyInBank(wrappedPlayer.asPlayer());
    }

    public static double getMoneyInBank(Player player){
        if(!econ.hasAccount(player))
            econ.createPlayerAccount(player);

        return econ.getBalance(player);
    }

    public static void depositMoney(Player player, double amount){
        econ.depositPlayer(player, amount);
    }

    public static void withdrawMoney(WrappedPlayer wrappedPlayer, double amount){
        withdrawMoney(wrappedPlayer.asPlayer(), amount);
    }

    public static void withdrawMoney(Player player, double amount){
        econ.withdrawPlayer(player, amount);
    }

    public static boolean isVaultEnabled(){
        return econ != null;
    }

}
