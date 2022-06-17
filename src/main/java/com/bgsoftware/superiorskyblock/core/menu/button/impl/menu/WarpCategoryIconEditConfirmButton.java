package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpCategoryIconEdit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WarpCategoryIconEditConfirmButton extends SuperiorMenuButton<MenuWarpCategoryIconEdit> {

    private WarpCategoryIconEditConfirmButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                              String requiredPermission, GameSound lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategoryIconEdit superiorMenu,
                              InventoryClickEvent clickEvent) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        WarpCategory warpCategory = superiorMenu.getIconProvider();

        EventResult<ItemStack> eventResult = plugin.getEventsBus().callIslandChangeWarpCategoryIconEvent(superiorPlayer,
                warpCategory.getIsland(), warpCategory, superiorMenu.getIconTemplate().build());

        if (eventResult.isCancelled())
            return;

        clickEvent.getWhoClicked().closeInventory();

        Message.WARP_CATEGORY_ICON_UPDATED.send(superiorPlayer);

        warpCategory.setIcon(eventResult.getResult());
    }

    public static class Builder extends AbstractBuilder<Builder, WarpCategoryIconEditConfirmButton, MenuWarpCategoryIconEdit> {

        @Override
        public WarpCategoryIconEditConfirmButton build() {
            return new WarpCategoryIconEditConfirmButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
