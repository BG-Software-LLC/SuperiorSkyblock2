package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;

public final class EconomyHook {

    private static final Economy econ;

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

    public static double getMoneyInBank(SuperiorPlayer superiorPlayer){
        return getMoneyInBank(superiorPlayer.asPlayer());
    }

    public static double getMoneyInBank(Player player){
        if(!econ.hasAccount(player))
            econ.createPlayerAccount(player);

        return econ.getBalance(player);
    }

    public static void depositMoney(Player player, BigDecimal amount){
        BigDecimal[] maximumsAndReminders = amount.divideAndRemainder(BigDecimal.valueOf(Double.MAX_VALUE));

        for(int i = 0; i < maximumsAndReminders[0].intValue(); i++){
            econ.depositPlayer(player, Double.MAX_VALUE);
        }

        econ.depositPlayer(player, maximumsAndReminders[1].doubleValue());
    }

    public static void withdrawMoney(SuperiorPlayer superiorPlayer, double amount){
        withdrawMoney(superiorPlayer.asPlayer(), amount);
    }

    public static void withdrawMoney(Player player, double amount){
        withdrawMoney(player, BigDecimal.valueOf(amount));
    }

    public static void withdrawMoney(Player player, BigDecimal amount){
        BigDecimal[] maximumsAndReminders = amount.divideAndRemainder(BigDecimal.valueOf(Double.MAX_VALUE));

        for(int i = 0; i < maximumsAndReminders[0].intValue(); i++){
            econ.withdrawPlayer(player, Double.MAX_VALUE);
        }

        econ.withdrawPlayer(player, maximumsAndReminders[1].doubleValue());

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isVaultEnabled(){
        return econ != null;
    }

}
