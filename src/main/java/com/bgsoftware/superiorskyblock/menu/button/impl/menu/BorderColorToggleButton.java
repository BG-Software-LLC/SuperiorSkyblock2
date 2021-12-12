package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuBorderColor;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BorderColorToggleButton extends SuperiorMenuButton {

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
    public ItemStack getButtonItem(SuperiorPlayer inventoryViewer, SuperiorPlayer targetPlayer) {
        ItemBuilder buttonItem = inventoryViewer.hasWorldBorderEnabled() ? enabledItem : disabledItem;
        return buttonItem.build(targetPlayer == null ? inventoryViewer : targetPlayer);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, SuperiorMenu superiorMenu, InventoryClickEvent clickEvent) {
        Preconditions.checkArgument(superiorMenu instanceof MenuBorderColor, "superiorMenu must be MenuBorderColor");

        plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), "toggle", "border");

        Executor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, BorderColorToggleButton> {

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
