package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuConfirmDisband;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class DisbandButton extends SuperiorMenuButton<MenuConfirmDisband> {

    private final boolean disbandIsland;

    private DisbandButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                          String requiredPermission, SoundWrapper lackPermissionSound, boolean disbandIsland) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.disbandIsland = disbandIsland;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuConfirmDisband superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island targetIsland = superiorMenu.getTargetIsland();

        if (disbandIsland && EventsCaller.callIslandDisbandEvent(clickedPlayer, targetIsland)) {
            IslandUtils.sendMessage(targetIsland, Message.DISBAND_ANNOUNCEMENT, new ArrayList<>(), clickedPlayer.getName());

            Message.DISBANDED_ISLAND.send(clickedPlayer);

            if (BuiltinModules.BANK.disbandRefund > 0 && targetIsland.getOwner().isOnline()) {
                Message.DISBAND_ISLAND_BALANCE_REFUND.send(targetIsland.getOwner(), StringUtils.format(targetIsland.getIslandBank()
                        .getBalance().multiply(BigDecimal.valueOf(BuiltinModules.BANK.disbandRefund))));
            }

            clickedPlayer.setDisbands(clickedPlayer.getDisbands() - 1);

            targetIsland.disbandIsland();
        }

        Executor.sync(superiorMenu::closePage, 1L);
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
