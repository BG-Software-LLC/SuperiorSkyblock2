package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Objects;

public class BorderColorButton extends AbstractMenuViewButton<BaseMenuView> {

    private BorderColorButton(AbstractMenuTemplateButton<BaseMenuView> templateButton, BaseMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        if (IslandUtils.handleBorderColorUpdate(menuView.getInventoryViewer(), getTemplate().borderColor))
            BukkitExecutor.sync(menuView::closeView, 1L);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<BaseMenuView> {

        private BorderColor borderColor;

        public Builder setBorderColor(BorderColor borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        @Override
        public MenuTemplateButton<BaseMenuView> build() {
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, borderColor);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<BaseMenuView> {

        private final BorderColor borderColor;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, BorderColor borderColor) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    BorderColorButton.class, BorderColorButton::new);
            this.borderColor = Objects.requireNonNull(borderColor, "borderColor cannot be null");
        }

    }

}
