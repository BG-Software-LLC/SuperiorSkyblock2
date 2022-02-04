package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategories;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class WarpCategoryButton extends SuperiorMenuButton<MenuWarpCategories> {

    private final WarpCategory warpCategory;

    private WarpCategoryButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                               String requiredPermission, SoundWrapper lackPermissionSound,
                               WarpCategory warpCategory) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.warpCategory = warpCategory;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategories superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        if (superiorMenu.hasManagePerms() && clickEvent.getClick().name().contains("RIGHT")) {
            superiorMenu.setPreviousMove(false);
            plugin.getMenus().openWarpCategoryManage(clickedPlayer, superiorMenu, warpCategory);
        } else {
            superiorMenu.setPreviousMove(false);
            plugin.getMenus().openWarps(clickedPlayer, superiorMenu, warpCategory);
        }
    }

    @Override
    public ItemStack getButtonItem(MenuWarpCategories superiorMenu) {
        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();
        Island island = superiorMenu.getTargetIsland();

        boolean isMember = island.isMember(inventoryViewer);
        long accessAmount = warpCategory.getWarps().stream().filter(
                islandWarp -> isMember || !islandWarp.hasPrivateFlag()
        ).count();

        if (accessAmount == 0)
            return null;

        if (!superiorMenu.hasManagePerms() || MenuWarpCategories.editLore.isEmpty()) {
            return warpCategory.getIcon(island.getOwner());
        } else {
            return new ItemBuilder(warpCategory.getIcon(null))
                    .appendLore(MenuWarpCategories.editLore)
                    .build(island.getOwner());
        }
    }

    public static class Builder extends AbstractBuilder<Builder, WarpCategoryButton, MenuWarpCategories> {

        private final WarpCategory warpCategory;

        public Builder(WarpCategory warpCategory) {
            this.warpCategory = warpCategory;
        }

        @Override
        public WarpCategoryButton build() {
            return new WarpCategoryButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, warpCategory);
        }

    }

}
