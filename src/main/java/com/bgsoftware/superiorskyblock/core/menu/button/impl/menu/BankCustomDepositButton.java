package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandBank;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.math.BigDecimal;
import java.util.List;

public class BankCustomDepositButton extends SuperiorMenuButton<MenuIslandBank> {

    private final GameSound successSound;
    private final GameSound failSound;

    private BankCustomDepositButton(TemplateItem buttonItem, List<String> commands,
                                    String requiredPermission, GameSound lackPermissionSound,
                                    GameSound successSound, GameSound failSound) {
        super(buttonItem, null, commands, requiredPermission, lackPermissionSound);
        this.successSound = successSound;
        this.failSound = failSound;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandBank superiorMenu, InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        Island island = superiorMenu.getTargetIsland();

        Message.BANK_DEPOSIT_CUSTOM.send(clickedPlayer);

        superiorMenu.closePage();

        PlayerChat.listen(player, message -> {
            try {
                BigDecimal newAmount = BigDecimal.valueOf(Double.parseDouble(message));
                BankTransaction bankTransaction = island.getIslandBank().depositMoney(clickedPlayer, newAmount);
                MenuIslandBank.handleDeposit(clickedPlayer, island, superiorMenu, bankTransaction,
                        successSound, failSound, newAmount);
            } catch (IllegalArgumentException ex) {
                Message.INVALID_AMOUNT.send(clickedPlayer, message);
            }

            PlayerChat.remove(player);

            MenuIslandBank.openInventory(clickedPlayer, null, clickedPlayer.getIsland());

            return true;
        });
    }

    public static class Builder extends AbstractBuilder<Builder, BankCustomDepositButton, MenuIslandBank> {

        private GameSound failSound;

        public Builder setSuccessSound(GameSound successSound) {
            this.clickSound = successSound;
            return this;
        }

        public Builder setFailSound(GameSound failSound) {
            this.failSound = failSound;
            return this;
        }

        @Override
        public BankCustomDepositButton build() {
            return new BankCustomDepositButton(buttonItem, commands, requiredPermission,
                    lackPermissionSound, clickSound, failSound);
        }

    }

}
