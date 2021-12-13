package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategoryIconEdit;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class WarpCategoryIconEditConfirmButton extends SuperiorMenuButton<MenuWarpCategoryIconEdit> {

    private WarpCategoryIconEditConfirmButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                              String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategoryIconEdit superiorMenu,
                              InventoryClickEvent clickEvent) {
        clickEvent.getWhoClicked().closeInventory();

        Locale.WARP_CATEGORY_ICON_UPDATED.send(clickEvent.getWhoClicked());

        superiorMenu.getWarpCategory().setIcon(superiorMenu.getItemBuilder().build());
    }

    public static class Builder extends AbstractBuilder<Builder, WarpCategoryIconEditConfirmButton, MenuWarpCategoryIconEdit> {

        @Override
        public WarpCategoryIconEditConfirmButton build() {
            return new WarpCategoryIconEditConfirmButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
