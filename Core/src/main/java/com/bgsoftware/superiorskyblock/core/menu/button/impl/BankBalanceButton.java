package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.impl.IslandMenuView;
import org.bukkit.inventory.ItemStack;

import java.util.Date;

public class BankBalanceButton extends AbstractMenuViewButton<IslandMenuView> {

    private BankBalanceButton(AbstractMenuTemplateButton<IslandMenuView> templateButton, IslandMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public ItemStack createViewItem() {
        Island island = menuView.getInventoryViewer().getIsland();
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        ItemBuilder itemBuilder = super.getButtonTemplateItem().getBuilder();

        return itemBuilder
                .replaceAll("{0}", island.getIslandBank().getBalance() + "")
                .replaceAll("{1}", Formatters.NUMBER_FORMATTER.format(island.getIslandBank().getBalance()))
                .replaceAll("{2}", Formatters.FANCY_NUMBER_FORMATTER.format(island.getIslandBank().getBalance(), inventoryViewer.getUserLocale()))
                .replaceAll("{3}", island.getBankLimit() + "")
                .replaceAll("{4}", Formatters.NUMBER_FORMATTER.format(island.getBankLimit()))
                .replaceAll("{5}", Formatters.FANCY_NUMBER_FORMATTER.format(island.getBankLimit(), inventoryViewer.getUserLocale()))
                .replaceAll("{6}", Formatters.DATE_FORMATTER.format(new Date(island.getLastInterestTime() * 1000L)))
                .replaceAll("{7}", Formatters.DATE_FORMATTER.format(new Date(System.currentTimeMillis() + island.getNextInterest() * 1000L)))
                .build();
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<IslandMenuView> {

        @Override
        public MenuTemplateButton<IslandMenuView> build() {
            return new MenuTemplateButtonImpl<>(this, BankBalanceButton.class,
                    BankBalanceButton::new);
        }

    }

}
