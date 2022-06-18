package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandBank;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.math.BigDecimal;
import java.util.List;

public class BankDepositButton extends SuperiorMenuButton<MenuIslandBank> {

    private final GameSound successSound;
    private final GameSound failSound;
    private final BigDecimal depositPercentage;

    private BankDepositButton(TemplateItem buttonItem, List<String> commands,
                              String requiredPermission, GameSound lackPermissionSound,
                              GameSound successSound, GameSound failSound, double depositPercentage) {
        super(buttonItem, null, commands, requiredPermission, lackPermissionSound);
        this.successSound = successSound;
        this.failSound = failSound;
        this.depositPercentage = BigDecimal.valueOf(depositPercentage / 100D);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandBank superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island island = superiorMenu.getTargetIsland();
        BigDecimal amount = plugin.getProviders().getBankEconomyProvider().getBalance(clickedPlayer).multiply(depositPercentage);
        BankTransaction bankTransaction = island.getIslandBank().depositMoney(clickedPlayer, amount);
        MenuIslandBank.handleDeposit(clickedPlayer, island, superiorMenu, bankTransaction,
                successSound, failSound, amount);
    }

    public static class Builder extends AbstractBuilder<Builder, BankDepositButton, MenuIslandBank> {

        private final double depositPercentage;
        private GameSound failSound;

        public Builder(double depositPercentage) {
            this.depositPercentage = depositPercentage;
        }

        public Builder setSuccessSound(GameSound successSound) {
            this.clickSound = successSound;
            return this;
        }

        public Builder setFailSound(GameSound failSound) {
            this.failSound = failSound;
            return this;
        }

        @Override
        public BankDepositButton build() {
            return new BankDepositButton(buttonItem, commands, requiredPermission,
                    lackPermissionSound, clickSound, failSound, depositPercentage);
        }

    }

}
