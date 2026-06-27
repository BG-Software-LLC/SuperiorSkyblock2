package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Objects;

public class OpenMissionCategoryButton extends AbstractMenuViewButton<BaseMenuView> {

    private OpenMissionCategoryButton(AbstractMenuTemplateButton<BaseMenuView> templateButton, BaseMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(ButtonClickContext<BaseMenuView> context) {
        if (getTemplate().requireIsland && !menuView.getInventoryViewer().hasIsland()) {
            Message.INVALID_ISLAND.send(menuView.getInventoryViewer());
            return;
        }

        menuView.setPreviousMove(false);
        plugin.getMenus().openMissionsCategory(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), getTemplate().missionCategory);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<BaseMenuView> {

        private MissionCategory missionCategory;

        public Builder setMissionsCategory(MissionCategory missionCategory) {
            this.missionCategory = missionCategory;
            return this;
        }

        @Override
        public MenuTemplateButton<BaseMenuView> build() {
            return new Template(this, missionCategory);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<BaseMenuView> {

        private final MissionCategory missionCategory;
        private final boolean requireIsland;

        Template(AbstractBuilder<BaseMenuView> builder, MissionCategory missionCategory) {
            super(builder, OpenMissionCategoryButton.class, OpenMissionCategoryButton::new);
            this.missionCategory = Objects.requireNonNull(missionCategory, "missionCategory cannot be null");
            this.requireIsland = !plugin.getMissions().isPlayerMissionCategory(missionCategory);
        }

    }

}
