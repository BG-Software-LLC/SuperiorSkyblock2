package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategoryIconEdit;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class WarpCategoryIconEditConfirmButton extends SuperiorMenuButton<MenuWarpCategoryIconEdit> {

    private WarpCategoryIconEditConfirmButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                                              String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategoryIconEdit superiorMenu,
                              InventoryClickEvent clickEvent) {
        clickEvent.getWhoClicked().closeInventory();

        Message.WARP_CATEGORY_ICON_UPDATED.send(clickEvent.getWhoClicked());

        superiorMenu.getIconProvider().setIcon(superiorMenu.getIconTemplate().build());
    }

    public static class Builder extends AbstractBuilder<Builder, WarpCategoryIconEditConfirmButton, MenuWarpCategoryIconEdit> {

        @Override
        public WarpCategoryIconEditConfirmButton build() {
            return new WarpCategoryIconEditConfirmButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
