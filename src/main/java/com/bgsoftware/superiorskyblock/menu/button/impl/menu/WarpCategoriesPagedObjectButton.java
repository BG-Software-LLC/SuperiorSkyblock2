package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategories;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class WarpCategoriesPagedObjectButton extends PagedObjectButton<MenuWarpCategories, WarpCategory> {

    private WarpCategoriesPagedObjectButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                            String requiredPermission, SoundWrapper lackPermissionSound,
                                            ItemBuilder nullItem, int objectIndex) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem, objectIndex);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategories superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        if (superiorMenu.hasManagePerms() && clickEvent.getClick().name().contains("RIGHT")) {
            superiorMenu.setPreviousMove(false);
            plugin.getMenus().openWarpCategoryManage(clickedPlayer, superiorMenu, pagedObject);
        } else {
            superiorMenu.setPreviousMove(false);
            plugin.getMenus().openWarps(clickedPlayer, superiorMenu, pagedObject);
        }
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuWarpCategories superiorMenu,
                                      WarpCategory warpCategory) {
        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();
        Island island = superiorMenu.getTargetIsland();

        boolean isMember = island.isMember(inventoryViewer);
        long accessAmount = warpCategory.getWarps().stream().filter(
                islandWarp -> isMember || !islandWarp.hasPrivateFlag()
        ).count();

        if (accessAmount == 0)
            return buttonItem;

        if (!superiorMenu.hasManagePerms() || MenuWarpCategories.editLore.isEmpty()) {
            return warpCategory.getIcon(island.getOwner());
        } else {
            return new ItemBuilder(warpCategory.getIcon(null))
                    .appendLore(MenuWarpCategories.editLore)
                    .build(island.getOwner());
        }
    }

    public static class Builder extends PagedObjectBuilder<Builder, WarpCategoriesPagedObjectButton, MenuWarpCategories> {

        @Override
        public WarpCategoriesPagedObjectButton build() {
            return new WarpCategoriesPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getObjectIndex());
        }

    }

}
