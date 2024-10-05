package com.bgsoftware.superiorskyblock.core.zmenu.buttons.bank;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import com.bgsoftware.superiorskyblock.island.bank.BankManager;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.math.BigDecimal;
import java.util.List;

public class BankActionButton extends SuperiorButton {

    private final BigDecimal percentage;
    private final BankAction bankAction;
    private final List<String> withdrawCommands;

    public BankActionButton(SuperiorSkyblockPlugin plugin, BigDecimal percentage, BankAction bankAction, List<String> withdrawCommands) {
        super(plugin);
        this.percentage = percentage;
        this.bankAction = bankAction;
        this.withdrawCommands = withdrawCommands;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {

        SuperiorPlayer clickedPlayer = getSuperiorPlayer(player);
        Island island = getCache(player).getIsland();

        super.onClick(player, event, inventory, slot, placeholders);
        switch (bankAction) {

            case DEPOSIT:
                if (percentage.doubleValue() <= 0) {

                    Message.BANK_DEPOSIT_CUSTOM.send(clickedPlayer);
                    player.closeInventory();
                    PlayerChat.listen(player, message -> {
                        try {
                            BigDecimal newAmount = BigDecimal.valueOf(Double.parseDouble(message));
                            BankTransaction bankTransaction = island.getIslandBank().depositMoney(clickedPlayer, newAmount);
                            BankManager.handleDeposit(clickedPlayer, island, bankTransaction, null, null, newAmount);
                        } catch (IllegalArgumentException exception) {
                            Message.INVALID_AMOUNT.send(clickedPlayer, message);
                        }

                        PlayerChat.remove(player);
                        menuManager.openInventory(player, "island-bank");

                        return true;
                    });

                } else {

                    BigDecimal amount = plugin.getProviders().getBankEconomyProvider().getBalance(clickedPlayer).multiply(this.percentage);

                    BankTransaction bankTransaction = island.getIslandBank().depositMoney(clickedPlayer, amount);
                    BankManager.handleDeposit(clickedPlayer, island, bankTransaction, null, null, amount);
                }
                break;
            case WITHDRAW:
                if (percentage.doubleValue() <= 0) {

                    Message.BANK_WITHDRAW_CUSTOM.send(clickedPlayer);
                    player.closeInventory();

                    PlayerChat.listen(player, message -> {
                        try {
                            BigDecimal newAmount = BigDecimal.valueOf(Double.parseDouble(message));
                            BankTransaction bankTransaction = island.getIslandBank().withdrawMoney(clickedPlayer, newAmount, null);
                            BankManager.handleWithdraw(clickedPlayer, island, bankTransaction, null, null, newAmount);
                        } catch (IllegalArgumentException exception) {
                            Message.INVALID_AMOUNT.send(clickedPlayer, message);
                        }

                        PlayerChat.remove(player);
                        menuManager.openInventory(player, "island-bank");

                        return true;
                    });

                } else {
                    BigDecimal amount = island.getIslandBank().getBalance().multiply(this.percentage);

                    BankTransaction bankTransaction = island.getIslandBank().withdrawMoney(clickedPlayer, amount, this.withdrawCommands);
                    BankManager.handleWithdraw(clickedPlayer, island, bankTransaction, null, null, amount);
                }

                break;
        }

    }

    public enum BankAction {
        DEPOSIT, WITHDRAW
    }
}
