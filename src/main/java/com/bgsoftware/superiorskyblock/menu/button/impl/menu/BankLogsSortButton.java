package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuBankLogs;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Comparator;
import java.util.List;

public final class BankLogsSortButton extends SuperiorMenuButton {

    private final SortType sortType;

    private BankLogsSortButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                               String requiredPermission, SoundWrapper lackPermissionSound, SortType sortType) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.sortType = sortType;
    }

    @Override
    public void onButtonClick(SuperiorMenu superiorMenu, InventoryClickEvent clickEvent) {
        Preconditions.checkArgument(superiorMenu instanceof MenuBankLogs, "superiorMenu must be MenuBankLogs");

        MenuBankLogs menuBankLogs = (MenuBankLogs) superiorMenu;

        switch (sortType) {
            case TIME:
                menuBankLogs.setSorting(Comparator.comparingLong(BankTransaction::getTime));
                break;
            case MONEY:
                menuBankLogs.setSorting((o1, o2) -> o2.getAmount().compareTo(o1.getAmount()));
                break;
            default:
                return;
        }

        menuBankLogs.refreshPage();
    }

    public static class Builder extends AbstractBuilder<Builder, BankLogsSortButton> {

        private SortType sortType;

        public Builder setSortType(SortType sortType) {
            this.sortType = sortType;
            return this;
        }

        @Override
        public BankLogsSortButton build() {
            return new BankLogsSortButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, sortType);
        }

    }

    public enum SortType {

        TIME,
        MONEY

    }

}
