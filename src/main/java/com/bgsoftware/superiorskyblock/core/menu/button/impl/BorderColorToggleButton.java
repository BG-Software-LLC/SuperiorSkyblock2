package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Either;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.inventory.ItemStack;

public class BorderColorToggleButton extends AbstractMenuViewButton<BaseMenuView> {

    private BorderColorToggleButton(AbstractMenuTemplateButton<BaseMenuView> templateButton, BaseMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public ItemStack createViewItem() {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        TemplateItem buttonItem = (inventoryViewer.hasWorldBorderEnabled() ? getTemplate().enabledButton : getTemplate().disabledButton).getLeft();
        return buttonItem.build(inventoryViewer);
    }

    @Override
    public void onButtonClick(ButtonClickContext<BaseMenuView> context) {
        plugin.getCommands().dispatchSubCommand(context.getPlayer(), "toggle", "border");
        BukkitExecutor.sync(menuView::closeView, 1L);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<BaseMenuView> {

        private Either<TemplateItem, DialogButton> disabledButton;

        public Builder setEnabledButtonItem(TemplateItem enabledButtonItem) {
            this.buttonData = Either.left(enabledButtonItem);
            return this;
        }

        public Builder setEnabledButtonDialog(DialogButton enabledButtonDialog) {
            this.buttonData = Either.right(enabledButtonDialog);
            return this;
        }

        public Builder setDisabledButtonItem(TemplateItem disabledButtonItem) {
            this.disabledButton = Either.left(disabledButtonItem);
            return this;
        }

        public Builder setDisabledButtonDialog(DialogButton disabledButtonDialog) {
            this.disabledButton = Either.right(disabledButtonDialog);
            return this;
        }

        @Override
        public MenuTemplateButton<BaseMenuView> build() {
            Either<TemplateItem, DialogButton> enabledButton = buttonData;
            this.buttonData = null;
            try {
                return new Template(this, enabledButton, disabledButton);
            } finally {
                this.buttonData = enabledButton;
            }
        }

    }

    public static class Template extends MenuTemplateButtonImpl<BaseMenuView> {

        private final Either<TemplateItem, DialogButton> enabledButton;
        private final Either<TemplateItem, DialogButton> disabledButton;

        Template(AbstractBuilder<BaseMenuView> builder,
                 @Nullable Either<TemplateItem, DialogButton> enabledButton,
                 @Nullable Either<TemplateItem, DialogButton> disabledButton) {
            super(builder, BorderColorToggleButton.class, BorderColorToggleButton::new);
            this.enabledButton = enabledButton == null ? Either.left(TemplateItem.AIR) : enabledButton;
            this.disabledButton = disabledButton == null ? Either.left(TemplateItem.AIR) : disabledButton;
        }

    }

}
