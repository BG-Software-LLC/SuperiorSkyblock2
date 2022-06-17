package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuBorderColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BorderColorToggleButton extends SuperiorMenuButton<MenuBorderColor> {

    private final TemplateItem enabledItem;
    private final TemplateItem disabledItem;

    private BorderColorToggleButton(TemplateItem enabledItem, GameSound clickSound, List<String> commands,
                                    String requiredPermission, GameSound lackPermissionSound,
                                    TemplateItem disabledItem) {
        super(null, clickSound, commands, requiredPermission, lackPermissionSound);
        this.enabledItem = enabledItem;
        this.disabledItem = disabledItem;
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(MenuBorderColor superiorMenu) {
        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();
        TemplateItem buttonItem = inventoryViewer.hasWorldBorderEnabled() ? enabledItem : disabledItem;
        return buttonItem.build(inventoryViewer);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuBorderColor superiorMenu, InventoryClickEvent clickEvent) {
        plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), "toggle", "border");
        BukkitExecutor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, BorderColorToggleButton, MenuBorderColor> {

        private TemplateItem disabledItem;

        public Builder setEnabledItem(TemplateItem enabledItem) {
            this.buttonItem = enabledItem;
            return this;
        }

        public Builder setDisabledItem(TemplateItem disabledItem) {
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
