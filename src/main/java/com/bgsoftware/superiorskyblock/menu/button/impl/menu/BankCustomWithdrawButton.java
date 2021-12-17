package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandBank;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.math.BigDecimal;
import java.util.List;

public final class BankCustomWithdrawButton extends SuperiorMenuButton<MenuIslandBank> {

    private final SoundWrapper successSound;
    private final SoundWrapper failSound;

    private BankCustomWithdrawButton(ItemBuilder buttonItem, List<String> commands,
                                     String requiredPermission, SoundWrapper lackPermissionSound,
                                     SoundWrapper successSound, SoundWrapper failSound) {
        super(buttonItem, null, commands, requiredPermission, lackPermissionSound);
        this.successSound = successSound;
        this.failSound = failSound;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandBank superiorMenu, InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        Island island = superiorMenu.getTargetIsland();

        Locale.BANK_WITHDRAW_CUSTOM.send(clickedPlayer);

        superiorMenu.closePage();

        PlayerChat.listen(player, message -> {
            try {
                BigDecimal newAmount = BigDecimal.valueOf(Double.parseDouble(message));
                BankTransaction bankTransaction = island.getIslandBank().withdrawMoney(clickedPlayer, newAmount, null);
                MenuIslandBank.handleWithdraw(clickedPlayer, island, superiorMenu, bankTransaction,
                        successSound, failSound, newAmount);
            } catch (IllegalArgumentException ex) {
                Locale.INVALID_AMOUNT.send(clickedPlayer, message);
            }

            PlayerChat.remove(player);

            MenuIslandBank.openInventory(clickedPlayer, null, clickedPlayer.getIsland());

            return true;
        });
    }

    public static class Builder extends AbstractBuilder<Builder, BankCustomWithdrawButton, MenuIslandBank> {

        private SoundWrapper failSound;

        public Builder setSuccessSound(SoundWrapper successSound) {
            this.clickSound = successSound;
            return this;
        }

        public Builder setFailSound(SoundWrapper failSound) {
            this.failSound = failSound;
            return this;
        }

        @Override
        public BankCustomWithdrawButton build() {
            return new BankCustomWithdrawButton(buttonItem, commands, requiredPermission,
                    lackPermissionSound, clickSound, failSound);
        }

    }

}
