package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarps;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class WarpPagedObjectButton extends PagedObjectButton<MenuWarps, IslandWarp> {

    private WarpPagedObjectButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                  String requiredPermission, SoundWrapper lackPermissionSound,
                                  ItemBuilder nullItem, int objectIndex) {
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
                .replaceAll("{1}", SBlockPosition.of(islandWarp.getLocation()).toString())
                .replaceAll("{2}", islandWarp.hasPrivateFlag() ?
                        ensureNotNull(Locale.ISLAND_WARP_PRIVATE.getMessage(superiorPlayer.getUserLocale())) :
                        ensureNotNull(Locale.ISLAND_WARP_PUBLIC.getMessage(superiorPlayer.getUserLocale())))
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
            Executor.sync(() -> superiorMenu.setPreviousMove(false), 1L);
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
