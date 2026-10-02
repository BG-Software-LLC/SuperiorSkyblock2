package com.bgsoftware.superiorskyblock.island.upgrade.cost;

import com.bgsoftware.superiorskyblock.commands.CommandsManagerImpl;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlaceholdersUpgradeCost extends UpgradeCostAbstract {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
        @Override
        protected PlaceholdersService create() {
            return plugin.getServices().getService(PlaceholdersService.class);
        }
    };

    private final String placeholder;
    private final List<String> withdrawCommands;

    public PlaceholdersUpgradeCost(BigDecimal cost, String placeholder, List<String> withdrawCommands) {
        super(cost, "placeholders");
        this.placeholder = placeholder;
        this.withdrawCommands = Collections.unmodifiableList(withdrawCommands);
    }

    @Override
    public boolean hasEnoughBalance(SuperiorPlayer superiorPlayer) {
        BigDecimal currentBalance = BigDecimal.ZERO;

        try {
            currentBalance = new BigDecimal(placeholdersService.get().parsePlaceholders(superiorPlayer.asOfflinePlayer(), placeholder));
        } catch (Exception ignored) {
        }

        return currentBalance.compareTo(cost) >= 0;
    }

    @Override
    public void withdrawCost(SuperiorPlayer superiorPlayer) {
        String cost = super.cost.toPlainString();
        String playerName = superiorPlayer.getName();
        withdrawCommands.forEach(command -> CommandsManagerImpl.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%amount%", cost).replace("%player%", playerName)));
    }

    @Override
    public CompletableFuture<Void> withdrawCostAsync(SuperiorPlayer superiorPlayer) {
        String cost = super.cost.toPlainString();
        String playerName = superiorPlayer.getName();
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        for (String command : this.withdrawCommands) {
            String parsedCommand = command.replace("%amount%", cost).replace("%player%", playerName);
            future = future.thenCompose(ignored -> CommandsManagerImpl.dispatchCommand(
                    Bukkit.getConsoleSender(), parsedCommand).thenApply(result -> null));
        }
        return future;
    }

    @Override
    public UpgradeCost clone(BigDecimal cost) {
        return new PlaceholdersUpgradeCost(cost, placeholder, withdrawCommands);
    }

}
