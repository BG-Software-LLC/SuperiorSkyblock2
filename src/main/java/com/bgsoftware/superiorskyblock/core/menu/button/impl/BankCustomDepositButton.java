package com.bgsoftware.superiorskyblock.core.menu.button.impl;

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
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.math.BigDecimal;
import java.util.List;

public class BankCustomDepositButton extends AbstractMenuViewButton<IslandMenuView> {

    private BankCustomDepositButton(AbstractMenuTemplateButton<IslandMenuView> templateButton, IslandMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        Island island = menuView.getIsland();

        Message.BANK_DEPOSIT_CUSTOM.send(clickedPlayer);

        menuView.closeView();

        PlayerChat.listen(player, message -> {
            try {
                BigDecimal newAmount = BigDecimal.valueOf(Double.parseDouble(message));
                BankTransaction bankTransaction = island.getIslandBank().depositMoney(clickedPlayer, newAmount);
                Menus.MENU_ISLAND_BANK.handleDeposit(clickedPlayer, island, bankTransaction,
                        getTemplate().successSound, getTemplate().failSound, newAmount);
            } catch (IllegalArgumentException ex) {
                Message.INVALID_AMOUNT.send(clickedPlayer, message);
            }

            PlayerChat.remove(player);

            plugin.getMenus().openIslandBank(clickedPlayer, null, clickedPlayer.getIsland());

            return true;
        });
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<IslandMenuView> {

        private GameSound failSound;
        private GameSound successSound;

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
            return new Template(buttonItem, commands, requiredPermission, lackPermissionSound, successSound, failSound);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<IslandMenuView> {

        private final GameSound successSound;
        private final GameSound failSound;

        Template(TemplateItem buttonItem, List<String> commands, String requiredPermission,
                 GameSound lackPermissionSound, GameSound successSound, GameSound failSound) {
            super(buttonItem, null, commands, requiredPermission, lackPermissionSound,
                    BankCustomDepositButton.class, BankCustomDepositButton::new);
            this.successSound = successSound;
            this.failSound = failSound;
        }

    }

}
