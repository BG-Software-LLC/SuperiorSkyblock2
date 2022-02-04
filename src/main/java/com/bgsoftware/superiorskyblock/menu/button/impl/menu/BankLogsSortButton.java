package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuBankLogs;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Comparator;
import java.util.List;

public final class BankLogsSortButton extends SuperiorMenuButton<MenuBankLogs> {

    private final SortType sortType;

    private BankLogsSortButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                               String requiredPermission, SoundWrapper lackPermissionSound, SortType sortType) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.sortType = sortType;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuBankLogs superiorMenu, InventoryClickEvent clickEvent) {
        switch (sortType) {
            case TIME:
                superiorMenu.setSorting(Comparator.comparingLong(BankTransaction::getTime));
                break;
            case MONEY:
                superiorMenu.setSorting((o1, o2) -> o2.getAmount().compareTo(o1.getAmount()));
                break;
            default:
                return;
        }

        superiorMenu.refreshPage();
    }

    public static class Builder extends AbstractBuilder<Builder, BankLogsSortButton, MenuBankLogs> {

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
