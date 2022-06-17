package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpManage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WarpManageLocationButton extends SuperiorMenuButton<MenuWarpManage> {

    private WarpManageLocationButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                     String requiredPermission, GameSound lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpManage superiorMenu,
                              InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();
        IslandWarp islandWarp = superiorMenu.getIslandWarp();

        Location warpLocation = player.getLocation();

        if (!islandWarp.getIsland().isInsideRange(warpLocation)) {
            Message.SET_WARP_OUTSIDE.send(player);
            return;
        }

        EventResult<Location> eventResult = plugin.getEventsBus().callIslandChangeWarpLocationEvent(
                plugin.getPlayers().getSuperiorPlayer(player), islandWarp.getIsland(), islandWarp, warpLocation);

        if (eventResult.isCancelled())
            return;

        Message.WARP_LOCATION_UPDATE.send(player);

        if (!islandWarp.getLocation().equals(eventResult.getResult())) {
            Block signBlock = islandWarp.getLocation().getBlock();
            if (signBlock.getState() instanceof Sign) {
                signBlock.setType(Material.AIR);
                signBlock.getWorld().dropItemNaturally(signBlock.getLocation(), new ItemStack(Material.SIGN));
                Message.DELETE_WARP_SIGN_BROKE.send(player);
            }
        }

        islandWarp.setLocation(eventResult.getResult());

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
