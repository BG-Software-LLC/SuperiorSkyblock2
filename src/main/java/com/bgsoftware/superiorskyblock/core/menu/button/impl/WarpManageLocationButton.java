package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.WorldPosition;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.warp.SignWarp;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

public class WarpManageLocationButton extends AbstractMenuViewButton<MenuWarpManage.View> {

    private WarpManageLocationButton(AbstractMenuTemplateButton<MenuWarpManage.View> templateButton, MenuWarpManage.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();
        IslandWarp islandWarp = menuView.getIslandWarp();

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location playerLocation = player.getLocation(wrapper.getHandle());

            if (!islandWarp.getIsland().isInsideRange(playerLocation)) {
                Message.SET_WARP_OUTSIDE.send(player);
                return;
            }

            PluginEvent<PluginEventArgs.IslandChangeWarpLocation> event = PluginEventsFactory.callIslandChangeWarpLocationEvent(
                    islandWarp.getIsland(), player, islandWarp, playerLocation);

            if (event.isCancelled())
                return;

            Message.WARP_LOCATION_UPDATE.send(player);

            WorldPosition warpPosition = islandWarp.getPosition();
            WorldInfo warpWorld = plugin.getGrid().getIslandsWorldInfo(islandWarp.getIsland(), islandWarp.getPositionDimension());

            if (!isSameLocation(event.getArgs().location, warpWorld, warpPosition)) {
                ChunksProvider.loadChunk(ChunkPosition.of(warpWorld, warpPosition), ChunkLoadReason.WARP_SIGN_BREAK, chunk -> {
                    SignWarp.trySignWarpBreak(islandWarp, player);
                });
            }

            islandWarp.setLocation(event.getArgs().location);

            GameSoundImpl.playSound(player, Menus.MENU_WARP_MANAGE.getSuccessUpdateSound());
        }
    }

    private static boolean isSameLocation(Location location, WorldInfo worldInfo, WorldPosition worldPosition) {
        if (location.getX() != worldPosition.getX() || location.getY() != worldPosition.getY() ||
                location.getZ() != worldPosition.getZ() || location.getYaw() != worldPosition.getYaw() ||
                location.getPitch() != worldPosition.getPitch())
            return false;
        String worldName = LazyWorldLocation.getWorldName(location);
        return Objects.equals(worldName, worldInfo.getName());
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuWarpManage.View> {

        @Override
        public MenuTemplateButton<MenuWarpManage.View> build() {
            return new MenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, WarpManageLocationButton.class, WarpManageLocationButton::new);
        }

    }

}
