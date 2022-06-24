package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmDisband;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class DisbandButton extends SuperiorMenuButton<MenuConfirmDisband> {

    private final boolean disbandIsland;

    private DisbandButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                          String requiredPermission, GameSound lackPermissionSound, boolean disbandIsland) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.disbandIsland = disbandIsland;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuConfirmDisband superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island targetIsland = superiorMenu.getTargetIsland();

        if (disbandIsland && plugin.getEventsBus().callIslandDisbandEvent(clickedPlayer, targetIsland)) {
            IslandUtils.sendMessage(targetIsland, Message.DISBAND_ANNOUNCEMENT, Collections.emptyList(), clickedPlayer.getName());

            Message.DISBANDED_ISLAND.send(clickedPlayer);

            if (BuiltinModules.BANK.disbandRefund > 0 && targetIsland.getOwner().isOnline()) {
                Message.DISBAND_ISLAND_BALANCE_REFUND.send(targetIsland.getOwner(), Formatters.NUMBER_FORMATTER.format(
                        targetIsland.getIslandBank().getBalance().multiply(BigDecimal.valueOf(BuiltinModules.BANK.disbandRefund))));
            }

            clickedPlayer.setDisbands(clickedPlayer.getDisbands() - 1);

            targetIsland.disbandIsland();
        }

        BukkitExecutor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, DisbandButton, MenuConfirmDisband> {

        private boolean disbandIsland;

        public Builder setDisbandIsland(boolean disbandIsland) {
            this.disbandIsland = disbandIsland;
            return this;
        }

        @Override
        public DisbandButton build() {
            return new DisbandButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, disbandIsland);
        }

    }

}
