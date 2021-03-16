package com.bgsoftware.superiorskyblock.upgrades.cost;

import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class PlaceholdersUpgradeCost extends UpgradeCostAbstract {

    private final String placeholder;
    private final List<String> withdrawCommands;

    public PlaceholdersUpgradeCost(BigDecimal cost, String placeholder, List<String> withdrawCommands){
        super(cost, "placeholders");
        this.placeholder = placeholder;
        this.withdrawCommands = Collections.unmodifiableList(withdrawCommands);
    }

    @Override
    public boolean hasEnoughBalance(SuperiorPlayer superiorPlayer) {
        BigDecimal currentBalance = BigDecimal.ZERO;

        try{
            currentBalance = new BigDecimal(PlaceholderHook.parse(superiorPlayer, placeholder));
        }catch (Exception ignored){}

        return currentBalance.compareTo(cost) >= 0;
    }

    @Override
    public void withdrawCost(SuperiorPlayer superiorPlayer) {
        String cost = super.cost.toPlainString();
        String playerName = superiorPlayer.getName();
        withdrawCommands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%amount%", cost).replace("%player%", playerName)));
    }

    @Override
    public UpgradeCost clone(BigDecimal cost) {
        return new PlaceholdersUpgradeCost(cost, placeholder, withdrawCommands);
    }

}
