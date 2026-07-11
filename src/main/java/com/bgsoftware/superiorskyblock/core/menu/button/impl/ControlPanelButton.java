package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.impl.IslandMenuView;

import java.util.Objects;

public class ControlPanelButton extends AbstractMenuViewButton<IslandMenuView> {

    private ControlPanelButton(AbstractMenuTemplateButton<IslandMenuView> templateButton, IslandMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(ButtonClickContext<IslandMenuView> context) {
        switch (getTemplate().controlPanelAction) {
            case OPEN_MEMBERS:
                plugin.getCommands().dispatchSubCommand(context.getPlayer(), "members");
                break;
            case OPEN_SETTINGS:
                plugin.getCommands().dispatchSubCommand(context.getPlayer(), "settings");
                break;
            case OPEN_VISITORS:
                plugin.getCommands().dispatchSubCommand(context.getPlayer(), "visitors");
                break;
        }
    }

    public enum ControlPanelAction {

        OPEN_MEMBERS,
        OPEN_SETTINGS,
        OPEN_VISITORS

    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<IslandMenuView> {

        private ControlPanelAction controlPanelAction;

        public Builder setAction(ControlPanelAction controlPanelAction) {
            this.controlPanelAction = controlPanelAction;
            return this;
        }

        @Override
        public MenuTemplateButton<IslandMenuView> build() {
            return new Template(this, controlPanelAction);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<IslandMenuView> {

        private final ControlPanelAction controlPanelAction;

        Template(AbstractBuilder<IslandMenuView> builder, ControlPanelAction controlPanelAction) {
            super(builder, ControlPanelButton.class, ControlPanelButton::new);
            this.controlPanelAction = Objects.requireNonNull(controlPanelAction, "controlPanelAction cannot be null");
        }

    }

}
