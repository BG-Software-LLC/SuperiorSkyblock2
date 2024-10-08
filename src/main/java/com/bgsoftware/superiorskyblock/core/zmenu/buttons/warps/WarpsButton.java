package com.bgsoftware.superiorskyblock.core.zmenu.buttons.warps;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.zmenu.PlayerCache;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.warp.SIslandWarp;
import fr.maxlego08.menu.MenuItemStack;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.ItemButton;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class WarpsButton extends SuperiorButton implements PaginateButton {

    private final MenuItemStack editItemStack;

    public WarpsButton(Plugin plugin, MenuItemStack editItemStack) {
        super((SuperiorSkyblockPlugin) plugin);
        this.editItemStack = editItemStack;
    }

    @Override
    public boolean hasPermission() {
        return true;
    }

    @Override
    public boolean checkPermission(Player player, InventoryDefault inventory, Placeholders placeholders) {
        return getPaginationSize(player) != 0;
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {

        Pagination<IslandWarp> pagination = new Pagination<>();
        List<IslandWarp> islandWarps = pagination.paginate(requestObjects(player), this.slots.size(), inventory.getPage());

        WarpCategory warpCategory = getCache(player).getWarpCategory();
        SuperiorPlayer superiorPlayer = getSuperiorPlayer(player);
        boolean hasPermissionEdit = warpCategory.getIsland().hasPermission(superiorPlayer, IslandPrivileges.SET_WARP);

        for (int i = 0; i != Math.min(islandWarps.size(), this.slots.size()); i++) {
            int slot = slots.get(i);

            IslandWarp islandWarp = islandWarps.get(i);
            Placeholders placeholders = new Placeholders();
            placeholders.register("material", islandWarp.getRawIcon() == null ? "BOOK" : islandWarp.getRawIcon().getType().name());
            placeholders.register("name", islandWarp.getName());
            placeholders.register("location", Formatters.LOCATION_FORMATTER.format(islandWarp.getLocation()));
            placeholders.register("status", islandWarp.hasPrivateFlag() ? ensureNotNull(Message.ISLAND_WARP_PRIVATE.getMessage(superiorPlayer.getUserLocale())) : ensureNotNull(Message.ISLAND_WARP_PUBLIC.getMessage(superiorPlayer.getUserLocale())));

            ItemButton itemButton = inventory.addItem(slot, (hasPermissionEdit ? this.editItemStack : getItemStack()).build(player, false, placeholders));
            if (hasPermissionEdit) {
                itemButton.setRightClick(event -> {
                    menuManager.openInventory(player, "warp-manage", cache -> cache.setIslandWarp(islandWarp));
                });
                itemButton.setLeftClick(event -> SIslandWarp.teleportWarp(plugin, superiorPlayer, warpCategory.getIsland(), islandWarp));
            } else {
                itemButton.setClick(event -> SIslandWarp.teleportWarp(plugin, superiorPlayer, warpCategory.getIsland(), islandWarp));
            }
        }
    }

    private String ensureNotNull(String check) {
        return check == null ? "" : check;
    }

    @Override
    public int getPaginationSize(Player player) {
        return requestObjects(player).size();
    }

    protected List<IslandWarp> requestObjects(Player player) {
        PlayerCache cache = getCache(player);
        WarpCategory warpCategory = cache.getWarpCategory();
        boolean isMember = warpCategory.getIsland().isMember(getSuperiorPlayer(player));
        return new SequentialListBuilder<IslandWarp>().filter(islandWarp -> isMember || !islandWarp.hasPrivateFlag()).build(warpCategory.getWarps());
    }
}
