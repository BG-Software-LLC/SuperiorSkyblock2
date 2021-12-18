package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class WarpManageLocationButton extends SuperiorMenuButton<MenuWarpManage> {

    private WarpManageLocationButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                     String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpManage superiorMenu,
                              InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();
        IslandWarp islandWarp = superiorMenu.getIslandWarp();

        if (!islandWarp.getIsland().isInsideRange(player.getLocation())) {
            Message.SET_WARP_OUTSIDE.send(player);
            return;
        }

        Message.WARP_LOCATION_UPDATE.send(player);

        Block signBlock = islandWarp.getLocation().getBlock();
        if (signBlock.getState() instanceof Sign) {
            signBlock.setType(Material.AIR);
            signBlock.getWorld().dropItemNaturally(signBlock.getLocation(), new ItemStack(Material.SIGN));
            Message.DELETE_WARP_SIGN_BROKE.send(player);
        }

        islandWarp.setLocation(player.getLocation());

        if (MenuWarpManage.successUpdateSound != null)
            MenuWarpManage.successUpdateSound.playSound(player);
    }

    public static class Builder extends AbstractBuilder<Builder, WarpManageLocationButton, MenuWarpManage> {

        @Override
        public WarpManageLocationButton build() {
            return new WarpManageLocationButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
