package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
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
    public void onButtonClick(InventoryClickEvent clickEvent) {
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
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, missionCategory);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<BaseMenuView> {

        private final MissionCategory missionCategory;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, MissionCategory missionCategory) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    OpenMissionCategoryButton.class, OpenMissionCategoryButton::new);
            this.missionCategory = Objects.requireNonNull(missionCategory, "missionCategory cannot be null");
        }

    }

}
