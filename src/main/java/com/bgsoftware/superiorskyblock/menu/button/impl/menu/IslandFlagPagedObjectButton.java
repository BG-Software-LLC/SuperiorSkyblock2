package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandFlags;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class IslandFlagPagedObjectButton extends PagedObjectButton<MenuIslandFlags, MenuIslandFlags.IslandFlagInfo> {

    private IslandFlagPagedObjectButton(ItemBuilder buttonItem, SoundWrapper clickSound,
                                        List<String> commands, String requiredPermission,
                                        SoundWrapper lackPermissionSound, ItemBuilder nullItem) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem);
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuIslandFlags superiorMenu,
                                      MenuIslandFlags.IslandFlagInfo islandFlagInfo) {
        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();
        Island island = superiorMenu.getTargetIsland();


        return island.hasSettingsEnabled(islandFlagInfo.getIslandFlag()) ?
                islandFlagInfo.getEnabledIslandFlagItem().build(inventoryViewer) :
                islandFlagInfo.getDisabledIslandFlagItem().build(inventoryViewer);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandFlags superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island island = superiorMenu.getTargetIsland();

        IslandFlag islandFlag = pagedObject.getIslandFlag();

        if (island.hasSettingsEnabled(islandFlag)) {
            island.disableSettings(islandFlag);
        } else {
            island.enableSettings(islandFlag);
        }

        Locale.UPDATED_SETTINGS.send(clickedPlayer, StringUtils.format(islandFlag.getName()));

        superiorMenu.refreshPage();
    }

    public static class Builder extends PagedObjectBuilder<Builder, IslandFlagPagedObjectButton, MenuIslandFlags> {

        @Override
        public IslandFlagPagedObjectButton build() {
            return new IslandFlagPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem);
        }

    }

}
