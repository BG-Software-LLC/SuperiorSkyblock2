package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
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
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ConfigEditorSaveButton extends AbstractMenuViewButton<MenuConfigEditor.View> {

    private static final TemplateItem SAVE_BUTTON_ITEM = new TemplateItem(new ItemBuilder(Material.EMERALD).withName("&aSave Changes"));

    private ConfigEditorSaveButton(AbstractMenuTemplateButton<MenuConfigEditor.View> templateButton, MenuConfigEditor.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();
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
            return new MenuTemplateButtonImpl<>(SAVE_BUTTON_ITEM, null, null, null,
                    null, ConfigEditorSaveButton.class, ConfigEditorSaveButton::new);
        }

    }

}
