package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.island.warp.SignWarp;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
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

        Location playerLocation = player.getLocation();

        if (!islandWarp.getIsland().isInsideRange(playerLocation)) {
            Message.SET_WARP_OUTSIDE.send(player);
            return;
        }

        EventResult<Location> eventResult = plugin.getEventsBus().callIslandChangeWarpLocationEvent(
                plugin.getPlayers().getSuperiorPlayer(player), islandWarp.getIsland(), islandWarp, playerLocation);

        if (eventResult.isCancelled())
            return;


        Message.WARP_LOCATION_UPDATE.send(player);

        Location warpLocation = islandWarp.getLocation();

        if (!warpLocation.equals(eventResult.getResult())) {
            ChunksProvider.loadChunk(ChunkPosition.of(warpLocation), ChunkLoadReason.WARP_SIGN_BREAK, chunk -> {
                SignWarp.trySignWarpBreak(islandWarp, player);
            });
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
