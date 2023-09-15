package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuBankLogs;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BankLogsSortButton extends AbstractMenuViewButton<MenuBankLogs.View> {

    private BankLogsSortButton(AbstractMenuTemplateButton<MenuBankLogs.View> templateButton, MenuBankLogs.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        getTemplate().sortType.onButtonClick(clickEvent, menuView);
        menuView.refreshView();
    }

    public enum SortType {

        TIME {
            @Override
            void onButtonClick(InventoryClickEvent clickEvent, MenuBankLogs.View menuView) {
                menuView.setSorting(Comparator.comparingLong(BankTransaction::getTime));
            }
        },
        MONEY {
            @Override
            void onButtonClick(InventoryClickEvent clickEvent, MenuBankLogs.View menuView) {
                menuView.setSorting((o1, o2) -> o2.getAmount().compareTo(o1.getAmount()));
            }
        };

        SortType() {

        }

        abstract void onButtonClick(InventoryClickEvent clickEvent, MenuBankLogs.View menuView);

    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuBankLogs.View> {

        private SortType sortType;

        public Builder setSortType(SortType sortType) {
            this.sortType = sortType;
            return this;
        }

        @Override
        public MenuTemplateButton<MenuBankLogs.View> build() {
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, sortType);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuBankLogs.View> {

        private final SortType sortType;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, SortType sortType) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    BankLogsSortButton.class, BankLogsSortButton::new);
            this.sortType = Objects.requireNonNull(sortType, "sortType cannot be null");
        }

    }

}
