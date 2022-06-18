package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarps;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WarpPagedObjectButton extends PagedObjectButton<MenuWarps, IslandWarp> {

    private WarpPagedObjectButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                  String requiredPermission, GameSound lackPermissionSound,
                                  TemplateItem nullItem, int objectIndex) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem, objectIndex);
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuWarps superiorMenu, IslandWarp islandWarp) {
        SuperiorPlayer superiorPlayer = superiorMenu.getInventoryViewer();

        ItemStack icon = islandWarp.getIcon(superiorPlayer);
        ItemBuilder itemBuilder = new ItemBuilder(icon == null ? buttonItem : icon);

        if (superiorMenu.hasManagePermission() && !MenuWarps.editLore.isEmpty())
            itemBuilder.appendLore(MenuWarps.editLore);

        return itemBuilder.replaceAll("{0}", islandWarp.getName())
                .replaceAll("{1}", Formatters.LOCATION_FORMATTER.format(islandWarp.getLocation()))
                .replaceAll("{2}", islandWarp.hasPrivateFlag() ?
                        ensureNotNull(Message.ISLAND_WARP_PRIVATE.getMessage(superiorPlayer.getUserLocale())) :
                        ensureNotNull(Message.ISLAND_WARP_PUBLIC.getMessage(superiorPlayer.getUserLocale())))
                .build(superiorPlayer);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarps superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        if (superiorMenu.hasManagePermission() && clickEvent.getClick().isRightClick()) {
            superiorMenu.setPreviousMove(false);
            plugin.getMenus().openWarpManage(clickedPlayer, superiorMenu, pagedObject);
        } else {
            MenuWarps.simulateClick(clickedPlayer, superiorMenu.getWarpCategory().getIsland(), pagedObject.getName());
            BukkitExecutor.sync(() -> superiorMenu.setPreviousMove(false), 1L);
        }
    }

    public static class Builder extends PagedObjectBuilder<Builder, WarpPagedObjectButton, MenuWarps> {

        @Override
        public WarpPagedObjectButton build() {
            return new WarpPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getObjectIndex());
        }

    }

    private static String ensureNotNull(String check) {
        return check == null ? "" : check;
    }

}
