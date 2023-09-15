package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.IslandMenuView;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.math.BigDecimal;
import java.util.List;

public class BankDepositButton extends AbstractMenuViewButton<IslandMenuView> {

    private BankDepositButton(AbstractMenuTemplateButton<IslandMenuView> templateButton, IslandMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        Island island = menuView.getIsland();

        BigDecimal amount = plugin.getProviders().getBankEconomyProvider().getBalance(clickedPlayer)
                .multiply(getTemplate().depositPercentage);

        BankTransaction bankTransaction = island.getIslandBank().depositMoney(clickedPlayer, amount);
        Menus.MENU_ISLAND_BANK.handleDeposit(clickedPlayer, island, bankTransaction,
                getTemplate().successSound, getTemplate().failSound, amount);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<IslandMenuView> {

        private final double depositPercentage;
        private GameSound successSound;
        private GameSound failSound;

        public Builder(double depositPercentage) {
            this.depositPercentage = depositPercentage;
        }

        public Builder setSuccessSound(GameSound successSound) {
            this.successSound = successSound;
            return this;
        }

        public Builder setFailSound(GameSound failSound) {
            this.failSound = failSound;
            return this;
        }

        @Override
        public MenuTemplateButton<IslandMenuView> build() {
            return new Template(buttonItem, commands, requiredPermission, lackPermissionSound,
                    successSound, failSound, depositPercentage);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<IslandMenuView> {

        @Nullable
        private final GameSound successSound;
        @Nullable
        private final GameSound failSound;
        private final BigDecimal depositPercentage;

        Template(@Nullable TemplateItem buttonItem, @Nullable List<String> commands, @Nullable String requiredPermission,
                 @Nullable GameSound lackPermissionSound, @Nullable GameSound successSound,
                 @Nullable GameSound failSound, double depositPercentage) {
            super(buttonItem, null, commands, requiredPermission, lackPermissionSound,
                    BankDepositButton.class, BankDepositButton::new);
            this.successSound = successSound;
            this.failSound = failSound;
            this.depositPercentage = BigDecimal.valueOf(depositPercentage / 100D);
        }

    }

}
