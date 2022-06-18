package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuBankLogs;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class BankLogsPagedObjectButton extends PagedObjectButton<MenuBankLogs, BankTransaction> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private BankLogsPagedObjectButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                      String requiredPermission, GameSound lackPermissionSound,
                                      TemplateItem nullItem, int objectIndex) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem, objectIndex);
    }


    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuBankLogs superiorMenu, InventoryClickEvent clickEvent) {
        superiorMenu.setFilteredPlayer(pagedObject.getPlayer());
        superiorMenu.refreshPage();
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuBankLogs superiorMenu, BankTransaction transaction) {
        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();
        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", transaction.getPosition() + "")
                .replaceAll("{1}", getFilteredPlayerName(transaction.getPlayer() == null ? CONSOLE_UUID : transaction.getPlayer()))
                .replaceAll("{2}", (transaction.getAction() == BankAction.WITHDRAW_COMPLETED ?
                        Message.BANK_WITHDRAW_COMPLETED : Message.BANK_DEPOSIT_COMPLETED).getMessage(inventoryViewer.getUserLocale()))
                .replaceAll("{3}", transaction.getDate())
                .replaceAll("{4}", transaction.getAmount() + "")
                .replaceAll("{5}", Formatters.NUMBER_FORMATTER.format(transaction.getAmount()))
                .replaceAll("{6}", Formatters.FANCY_NUMBER_FORMATTER.format(transaction.getAmount(), inventoryViewer.getUserLocale()))
                .asSkullOf(inventoryViewer)
                .build(inventoryViewer);
    }

    private static String getFilteredPlayerName(UUID filteredPlayer) {
        if (filteredPlayer == null) {
            return "";
        } else if (filteredPlayer.equals(CONSOLE_UUID)) {
            return "Console";
        } else {
            return plugin.getPlayers().getSuperiorPlayer(filteredPlayer).getName();
        }
    }

    public static class Builder extends PagedObjectBuilder<Builder, BankLogsPagedObjectButton, MenuBankLogs> {

        @Override
        public BankLogsPagedObjectButton build() {
            return new BankLogsPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getObjectIndex());
        }

    }

}
