package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.IslandMenuView;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class DisbandButton extends AbstractMenuViewButton<IslandMenuView> {

    private DisbandButton(AbstractMenuTemplateButton<IslandMenuView> templateButton, IslandMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        Island targetIsland = menuView.getIsland();

        if (getTemplate().disbandIsland && plugin.getEventsBus().callIslandDisbandEvent(inventoryViewer, targetIsland)) {
            IslandUtils.sendMessage(targetIsland, Message.DISBAND_ANNOUNCEMENT, Collections.emptyList(), inventoryViewer.getName());

            Message.DISBANDED_ISLAND.send(inventoryViewer);

            if (BuiltinModules.BANK.disbandRefund > 0) {
                Message.DISBAND_ISLAND_BALANCE_REFUND.send(targetIsland.getOwner(), Formatters.NUMBER_FORMATTER.format(
                        targetIsland.getIslandBank().getBalance().multiply(BigDecimal.valueOf(BuiltinModules.BANK.disbandRefund))));
            }

            inventoryViewer.setDisbands(inventoryViewer.getDisbands() - 1);

            targetIsland.disbandIsland();
        }

        BukkitExecutor.sync(menuView::closeView, 1L);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<IslandMenuView> {

        private boolean disbandIsland;

        public Builder setDisbandIsland(boolean disbandIsland) {
            this.disbandIsland = disbandIsland;
            return this;
        }

        @Override
        public MenuTemplateButton<IslandMenuView> build() {
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, disbandIsland);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<IslandMenuView> {

        private final boolean disbandIsland;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, boolean disbandIsland) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    DisbandButton.class, DisbandButton::new);
            this.disbandIsland = disbandIsland;
        }

    }

}
