package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collections;
import java.util.List;

public class LeaveButton extends AbstractMenuViewButton<BaseMenuView> {

    private LeaveButton(AbstractMenuTemplateButton<BaseMenuView> templateButton, BaseMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        Island island = inventoryViewer.getIsland();

        if (getTemplate().leaveIsland && island != null && plugin.getEventsBus().callIslandQuitEvent(inventoryViewer, island)) {
            island.kickMember(inventoryViewer);

            IslandUtils.sendMessage(island, Message.LEAVE_ANNOUNCEMENT, Collections.emptyList(), inventoryViewer.getName());

            Message.LEFT_ISLAND.send(inventoryViewer);
        }

        BukkitExecutor.sync(menuView::closeView, 1L);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<BaseMenuView> {

        private boolean leaveIsland;

        public Builder setLeaveIsland(boolean leaveIsland) {
            this.leaveIsland = leaveIsland;
            return this;
        }

        @Override
        public MenuTemplateButton<BaseMenuView> build() {
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, leaveIsland);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<BaseMenuView> {

        private final boolean leaveIsland;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, boolean leaveIsland) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    LeaveButton.class, LeaveButton::new);
            this.leaveIsland = leaveIsland;
        }

    }

}
