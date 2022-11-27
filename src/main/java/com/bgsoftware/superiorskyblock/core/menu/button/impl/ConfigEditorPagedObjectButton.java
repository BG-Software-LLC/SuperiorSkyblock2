package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.MenuConfigEditor;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ConfigEditorPagedObjectButton extends AbstractPagedMenuButton<MenuConfigEditor.View, ItemStack> {

    private ConfigEditorPagedObjectButton(MenuTemplateButton<MenuConfigEditor.View> templateButton, MenuConfigEditor.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        MenuConfigEditor.View currentView = getView();
        SuperiorPlayer inventoryViewer = currentView.getInventoryViewer();
        Player player = inventoryViewer.asPlayer();

        if (player == null)
            return;

        try {
            String sectionPath = currentView.getPathSlots().get((currentView.getCurrentPage() - 1) * 36 + clickEvent.getRawSlot());

            if (sectionPath == null)
                return;

            String viewPath = currentView.getPath();
            String fullPath = viewPath.isEmpty() ? sectionPath : viewPath + "." + sectionPath;

            CommentedConfiguration config = Menus.MENU_CONFIG_EDITOR.getConfig();

            if (config.isConfigurationSection(fullPath)) {
                currentView.setPreviousMove(false);
                currentView.closeView();
                currentView.getMenu().createView(inventoryViewer, new MenuConfigEditor.Args(fullPath), currentView);
            } else if (config.isBoolean(fullPath)) {
                Menus.MENU_CONFIG_EDITOR.updateConfig(player, fullPath, !config.getBoolean(fullPath));
                currentView.refreshView();
            } else {
                PlayerChat.listen(player, message -> onPlayerChat(currentView, config, player, message, fullPath));

                currentView.closeView();

                player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

                if (config.isList(fullPath)) {
                    player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        return pagedObject;
    }

    private boolean onPlayerChat(MenuView<?, ?> lastView, CommentedConfiguration config, Player player, Object message, String path) {
        if (!message.toString().equalsIgnoreCase("-cancel")) {
            if (config.isList(path)) {
                List<String> list = config.getStringList(path);

                if (list.contains(message.toString())) {
                    list.remove(message.toString());
                    player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Removed the value " + message + " from " + path);
                } else {
                    list.add(message.toString());
                    player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Added the value " + message + " to " + path);
                }

                config.set(path, list);
            } else {
                boolean valid = true;
                if (config.isInt(path)) {
                    try {
                        message = Integer.valueOf(message.toString());
                    } catch (IllegalArgumentException ex) {
                        player.sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                } else if (config.isDouble(path)) {
                    try {
                        message = Double.valueOf(message.toString());
                    } catch (IllegalArgumentException ex) {
                        player.sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                if (valid) {
                    config.set(path, message);
                    Menus.MENU_CONFIG_EDITOR.updateConfig(player, path, message);
                }
            }
        }

        PlayerChat.remove(player);

        lastView.refreshView();

        return true;
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuConfigEditor.View, ItemStack> {

        @Override
        public PagedMenuTemplateButton<MenuConfigEditor.View, ItemStack> build() {
            return new PagedMenuTemplateButtonImpl<>(null, null, null, null,
                    null, null, getButtonIndex(),
                    ConfigEditorPagedObjectButton.class, ConfigEditorPagedObjectButton::new);
        }

    }

}
