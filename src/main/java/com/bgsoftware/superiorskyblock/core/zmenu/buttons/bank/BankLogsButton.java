package com.bgsoftware.superiorskyblock.core.zmenu.buttons.bank;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.zmenu.PlayerCache;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class BankLogsButton extends SuperiorButton implements PaginateButton {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    public BankLogsButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public void onInventoryOpen(Player player, InventoryDefault inventory, Placeholders placeholders) {
        super.onInventoryOpen(player, inventory, placeholders);

        PlayerCache playerCache = getCache(player);
        placeholders.register("player", getFilteredPlayerName(playerCache.getFilteredPlayer()));
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {

        Pagination<BankTransaction> pagination = new Pagination<>();
        List<BankTransaction> bankTransactions = pagination.paginate(this.getSortTransactions(player), this.slots.size(), inventory.getPage());
        SuperiorPlayer superiorPlayer = getSuperiorPlayer(player);

        for (int i = 0; i != Math.min(bankTransactions.size(), this.slots.size()); i++) {
            int slot = slots.get(i);
            BankTransaction bankTransaction = bankTransactions.get(i);

            Placeholders placeholders = new Placeholders();
            placeholders.register("transaction", String.valueOf((i + 1) + ((inventory.getPage() - 1) * this.slots.size())));
            placeholders.register("player", getFilteredPlayerName(bankTransaction.getPlayer() == null ? CONSOLE_UUID : bankTransaction.getPlayer()));
            placeholders.register("status", (bankTransaction.getAction() == BankAction.WITHDRAW_COMPLETED ? Message.BANK_WITHDRAW_COMPLETED : Message.BANK_DEPOSIT_COMPLETED).getMessage(superiorPlayer.getUserLocale()));
            placeholders.register("time", bankTransaction.getDate());
            placeholders.register("amount", String.valueOf(bankTransaction.getAmount()));
            placeholders.register("amount_formatted", Formatters.NUMBER_FORMATTER.format(bankTransaction.getAmount()));
            placeholders.register("amount_fancy", Formatters.FANCY_NUMBER_FORMATTER.format(bankTransaction.getAmount(), superiorPlayer.getUserLocale()));

            inventory.addItem(slot, getItemStack().build(player, false, placeholders)).setClick(event -> menuManager.openInventory(player, "bank-logs", cache -> cache.setFilteredPlayer(bankTransaction.getPlayer())));
        }
    }

    private List<BankTransaction> getSortTransactions(Player player) {

        List<BankTransaction> transactions = getTransactions(player);
        PlayerCache playerCache = getCache(player);

        if (playerCache.getBankSorting() == null) {
            return transactions;
        }

        transactions = new LinkedList<>(transactions);
        transactions.sort(playerCache.getBankSorting());

        return Collections.unmodifiableList(transactions);
    }

    @Override
    public int getPaginationSize(Player player) {
        return getTransactions(player).size();
    }

    private List<BankTransaction> getTransactions(Player player) {

        PlayerCache playerCache = getCache(player);
        UUID filteredPlayer = playerCache.getFilteredPlayer();
        Island island = playerCache.getIsland();

        if (filteredPlayer == null) {
            return island.getIslandBank().getAllTransactions();
        } else if (filteredPlayer.equals(CONSOLE_UUID)) {
            return island.getIslandBank().getConsoleTransactions();
        } else {
            return island.getIslandBank().getTransactions(plugin.getPlayers().getSuperiorPlayer(filteredPlayer));
        }
    }

    private String getFilteredPlayerName(UUID filteredPlayer) {
        if (filteredPlayer == null) {
            return "";
        } else if (filteredPlayer.equals(CONSOLE_UUID)) {
            return "Console";
        } else {
            return this.plugin.getPlayers().getSuperiorPlayer(filteredPlayer).getName();
        }
    }
}
