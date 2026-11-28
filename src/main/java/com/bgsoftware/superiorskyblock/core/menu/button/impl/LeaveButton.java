package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;

public class LeaveButton extends AbstractMenuViewButton<BaseMenuView> {

    private LeaveButton(AbstractMenuTemplateButton<BaseMenuView> templateButton, BaseMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(ButtonClickContext<BaseMenuView> context) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        Island island = inventoryViewer.getIsland();

        if (getTemplate().leaveIsland && island != null) {
            IslandUtils.handleLeaveIsland(inventoryViewer, island);
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
            return new Template(this, leaveIsland);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<BaseMenuView> {

        private final boolean leaveIsland;

        Template(AbstractBuilder<BaseMenuView> builder, boolean leaveIsland) {
            super(builder, LeaveButton.class, LeaveButton::new);
            this.leaveIsland = leaveIsland;
        }

    }

}
