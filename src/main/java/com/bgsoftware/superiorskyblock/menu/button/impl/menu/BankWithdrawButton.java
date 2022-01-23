package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandBank;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.math.BigDecimal;
import java.util.List;

public final class BankWithdrawButton extends SuperiorMenuButton<MenuIslandBank> {

    private final SoundWrapper successSound;
    private final SoundWrapper failSound;
    private final BigDecimal withdrawValue;
    private final List<String> withdrawCommands;

    private BankWithdrawButton(ItemBuilder buttonItem, List<String> commands, String requiredPermission,
                               SoundWrapper lackPermissionSound, SoundWrapper successSound, SoundWrapper failSound,
                               double withdrawValue, List<String> withdrawCommands) {
        super(buttonItem, null, commands, requiredPermission, lackPermissionSound);
        this.successSound = successSound;
        this.failSound = failSound;
        this.withdrawValue = BigDecimal.valueOf(withdrawValue / 100D);
        this.withdrawCommands = withdrawCommands;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandBank superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island island = superiorMenu.getTargetIsland();
        BigDecimal amount = island.getIslandBank().getBalance().multiply(withdrawValue);
        BankTransaction bankTransaction = island.getIslandBank().withdrawMoney(clickedPlayer, amount, withdrawCommands);
        MenuIslandBank.handleWithdraw(clickedPlayer, island, superiorMenu, bankTransaction,
                successSound, failSound, amount);
    }

    public static class Builder extends AbstractBuilder<Builder, BankWithdrawButton, MenuIslandBank> {

        private final double withdrawValue;
        private final List<String> withdrawCommands;
        private SoundWrapper failSound;

        public Builder(double withdrawValue) {
            this.withdrawValue = withdrawValue;
            this.withdrawCommands = null;
        }

        public Builder(List<String> withdrawCommands) {
            this.withdrawValue = 100D;
            this.withdrawCommands = withdrawCommands;
        }

        public Builder setSuccessSound(SoundWrapper successSound) {
            this.clickSound = successSound;
            return this;
        }

        public Builder setFailSound(SoundWrapper failSound) {
            this.failSound = failSound;
            return this;
        }

        @Override
        public BankWithdrawButton build() {
            return new BankWithdrawButton(buttonItem, commands, requiredPermission, lackPermissionSound, clickSound,
                    failSound, withdrawValue, withdrawCommands);
        }

    }

}
