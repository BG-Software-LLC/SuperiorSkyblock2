package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpIconEdit;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class WarpIconEditConfirmButton extends SuperiorMenuButton<MenuWarpIconEdit> {

    private WarpIconEditConfirmButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                                      String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpIconEdit superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        IslandWarp islandWarp = superiorMenu.getIconProvider();

        EventResult<ItemStack> eventResult = plugin.getEventsBus().callIslandChangeWarpIconEvent(superiorPlayer,
                islandWarp.getIsland(), islandWarp, superiorMenu.getIconTemplate().build());

        if (eventResult.isCancelled())
            return;

        clickEvent.getWhoClicked().closeInventory();

        Message.WARP_ICON_UPDATED.send(superiorPlayer);

        islandWarp.setIcon(eventResult.getResult());
    }

    public static class Builder extends AbstractBuilder<Builder, WarpIconEditConfirmButton, MenuWarpIconEdit> {

        @Override
        public WarpIconEditConfirmButton build() {
            return new WarpIconEditConfirmButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
