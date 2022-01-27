package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuBorderColor;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BorderColorToggleButton extends SuperiorMenuButton<MenuBorderColor> {

    private final ItemBuilder enabledItem;
    private final ItemBuilder disabledItem;

    private BorderColorToggleButton(ItemBuilder enabledItem, SoundWrapper clickSound, List<String> commands,
                                    String requiredPermission, SoundWrapper lackPermissionSound, ItemBuilder disabledItem) {
        super(null, clickSound, commands, requiredPermission, lackPermissionSound);
        this.enabledItem = enabledItem;
        this.disabledItem = disabledItem;
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(MenuBorderColor superiorMenu) {
        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();
        ItemBuilder buttonItem = inventoryViewer.hasWorldBorderEnabled() ? enabledItem : disabledItem;
        return buttonItem.copy().build(inventoryViewer);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuBorderColor superiorMenu, InventoryClickEvent clickEvent) {
        plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), "toggle", "border");
        Executor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, BorderColorToggleButton, MenuBorderColor> {

        private ItemBuilder disabledItem;

        public Builder setEnabledItem(ItemBuilder enabledItem) {
            this.buttonItem = enabledItem;
            return this;
        }

        public Builder setDisabledItem(ItemBuilder disabledItem) {
            this.disabledItem = disabledItem;
            return this;
        }

        @Override
        public BorderColorToggleButton build() {
            return new BorderColorToggleButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, disabledItem);
        }

    }

}
