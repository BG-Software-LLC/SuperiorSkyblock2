package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandChest;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class IslandChestPagedObjectButton extends PagedObjectButton<MenuIslandChest, IslandChest> {

    private IslandChestPagedObjectButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                         String requiredPermission, GameSound lackPermissionSound,
                                         TemplateItem nullItem, int objectIndex) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem, objectIndex);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandChest superiorMenu,
                              InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        superiorMenu.setPreviousMove(false);
        pagedObject.openChest(clickedPlayer);
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuIslandChest superiorMenu, IslandChest islandChest) {
        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();
        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", (islandChest.getIndex() + 1) + "")
                .replaceAll("{1}", (islandChest.getRows() * 9) + "")
                .build(inventoryViewer);
    }

    public static class Builder extends PagedObjectBuilder<Builder, IslandChestPagedObjectButton, MenuIslandChest> {

        @Override
        public IslandChestPagedObjectButton build() {
            return new IslandChestPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getObjectIndex());
        }

    }

}
