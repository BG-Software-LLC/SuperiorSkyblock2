package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategoryIconEdit;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class WarpCategoryIconDisplayButton extends SuperiorMenuButton<MenuWarpCategoryIconEdit> {

    private WarpCategoryIconDisplayButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                          String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategoryIconEdit superiorMenu,
                              InventoryClickEvent clickEvent) {
        // Dummy button
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(MenuWarpCategoryIconEdit superiorMenu) {
        ItemBuilder buttonItem = superiorMenu.getItemBuilder();
        return buttonItem == null ? null : buttonItem.clone().build();
    }

    public static class Builder extends AbstractBuilder<Builder, WarpCategoryIconDisplayButton, MenuWarpCategoryIconEdit> {

        @Override
        public WarpCategoryIconDisplayButton build() {
            return new WarpCategoryIconDisplayButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
