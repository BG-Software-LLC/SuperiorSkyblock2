package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.Either;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.MenuConfigEditor;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import org.bukkit.entity.Player;

public class ConfigEditorSaveButton extends AbstractMenuViewButton<MenuConfigEditor.View> {

    private static final TemplateItem SAVE_BUTTON_ITEM = new TemplateItem(new ItemBuilder(Material.EMERALD).withName("&aSave Changes"));

    private ConfigEditorSaveButton(AbstractMenuTemplateButton<MenuConfigEditor.View> templateButton, MenuConfigEditor.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuConfigEditor.View> context) {
        Player player = context.getPlayer();
        BukkitExecutor.async(() -> {
            Menus.MENU_CONFIG_EDITOR.saveConfig(config -> plugin.getSettings().loadData());
            player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Saved configuration successfully.");
            BukkitExecutor.sync(() -> {
                getView().setPreviousMove(false);
                getView().closeView();
            });
        });
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuConfigEditor.View> {

        @Override
        public MenuTemplateButton<MenuConfigEditor.View> build() {
            this.buttonData = Either.left(SAVE_BUTTON_ITEM);
            this.clickSound = null;
            this.commands = null;
            this.requiredPermission = null;
            this.lackPermissionSound = null;
            return new MenuTemplateButtonImpl<>(this, ConfigEditorSaveButton.class,
                    ConfigEditorSaveButton::new);
        }

    }

}
