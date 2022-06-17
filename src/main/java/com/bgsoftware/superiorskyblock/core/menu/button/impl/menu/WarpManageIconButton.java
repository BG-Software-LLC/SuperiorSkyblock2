package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.island.warp.SIslandWarp;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpManage;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WarpManageIconButton extends SuperiorMenuButton<MenuWarpManage> {

    private WarpManageIconButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                 String requiredPermission, GameSound lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpManage superiorMenu,
                              InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        superiorMenu.setPreviousMove(false);
        plugin.getMenus().openWarpIconEdit(clickedPlayer, superiorMenu, superiorMenu.getIslandWarp());
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(MenuWarpManage superiorMenu) {
        IslandWarp islandWarp = superiorMenu.getIslandWarp();

        ItemBuilder itemBuilder = islandWarp.getRawIcon() == null ?
                SIslandWarp.DEFAULT_WARP_ICON.getBuilder() : new ItemBuilder(islandWarp.getRawIcon());

        ItemStack buttonItem = super.getButtonItem(superiorMenu);

        if (buttonItem != null && buttonItem.hasItemMeta()) {
            ItemMeta itemMeta = buttonItem.getItemMeta();
            if (itemMeta.hasDisplayName())
                itemBuilder.withName(itemMeta.getDisplayName());

            if (itemMeta.hasLore())
                itemBuilder.appendLore(itemMeta.getLore());
        }

        return itemBuilder.build(superiorMenu.getInventoryViewer());
    }

    public static class Builder extends AbstractBuilder<Builder, WarpManageIconButton, MenuWarpManage> {

        @Override
        public WarpManageIconButton build() {
            return new WarpManageIconButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
