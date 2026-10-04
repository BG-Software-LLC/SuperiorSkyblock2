package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuBankLogs;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class BankLogsPagedObjectButton extends AbstractPagedMenuButton<MenuBankLogs.View, BankTransaction> {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private BankLogsPagedObjectButton(MenuTemplateButton<MenuBankLogs.View> templateButton, MenuBankLogs.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuBankLogs.View> context) {
        menuView.setFilteredPlayer(pagedObject.getPlayer());
        menuView.refreshView();
    }

    @Override
    public ItemStack modifyViewItem(ItemBuilder itemBuilder) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        return itemBuilder
                .replaceAll("{0}", pagedObject.getPosition() + "")
                .replaceAll("{1}", getFilteredPlayerName(pagedObject.getPlayer() == null ? CONSOLE_UUID : pagedObject.getPlayer()))
                .replaceAll("{2}", (pagedObject.getAction() == BankAction.WITHDRAW_COMPLETED ?
                        Message.BANK_WITHDRAW_COMPLETED : Message.BANK_DEPOSIT_COMPLETED).getMessage(inventoryViewer.getUserLocale()))
                .replaceAll("{3}", pagedObject.getDate())
                .replaceAll("{4}", pagedObject.getAmount() + "")
                .replaceAll("{5}", Formatters.NUMBER_FORMATTER.format(pagedObject.getAmount()))
                .replaceAll("{6}", Formatters.FANCY_NUMBER_FORMATTER.format(pagedObject.getAmount(), inventoryViewer.getUserLocale()))
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

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuBankLogs.View, BankTransaction> {

        @Override
        public PagedMenuTemplateButton<MenuBankLogs.View, BankTransaction> build() {
            return new PagedMenuTemplateButtonImpl<>(this, BankLogsPagedObjectButton.class,
                    BankLogsPagedObjectButton::new);
        }

    }

}
