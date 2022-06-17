package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandFlags;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class IslandFlagPagedObjectButton extends PagedObjectButton<MenuIslandFlags, MenuIslandFlags.IslandFlagInfo> {

    private IslandFlagPagedObjectButton(TemplateItem buttonItem, List<String> commands,
                                        String requiredPermission, GameSound lackPermissionSound,
                                        TemplateItem nullItem, int objectIndex) {
        super(buttonItem, null, commands, requiredPermission, lackPermissionSound, nullItem, objectIndex);
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuIslandFlags superiorMenu,
                                      MenuIslandFlags.IslandFlagInfo islandFlagInfo) {
        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();
        Island island = superiorMenu.getTargetIsland();

        IslandFlag islandFlag = islandFlagInfo.getIslandFlag();

        if (islandFlag == null)
            return buttonItem;

        return island.hasSettingsEnabled(islandFlag) ?
                islandFlagInfo.getEnabledIslandFlagItem().build(inventoryViewer) :
                islandFlagInfo.getDisabledIslandFlagItem().build(inventoryViewer);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandFlags superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island island = superiorMenu.getTargetIsland();

        IslandFlag islandFlag = pagedObject.getIslandFlag();

        if (islandFlag == null)
            return;

        if (island.hasSettingsEnabled(islandFlag)) {
            if (!plugin.getEventsBus().callIslandDisableFlagEvent(clickedPlayer, island, islandFlag))
                return;

            island.disableSettings(islandFlag);
        } else {
            if (!plugin.getEventsBus().callIslandEnableFlagEvent(clickedPlayer, island, islandFlag))
                return;

            island.enableSettings(islandFlag);
        }

        GameSound clickSound = pagedObject.getClickSound();
        if (clickSound != null)
            clickSound.playSound(clickEvent.getWhoClicked());

        Message.UPDATED_SETTINGS.send(clickedPlayer, Formatters.CAPITALIZED_FORMATTER.format(islandFlag.getName()));

        superiorMenu.refreshPage();
    }

    public static class Builder extends PagedObjectBuilder<Builder, IslandFlagPagedObjectButton, MenuIslandFlags> {

        @Override
        public IslandFlagPagedObjectButton build() {
            return new IslandFlagPagedObjectButton(buttonItem, commands, requiredPermission,
                    lackPermissionSound, nullItem, getObjectIndex());
        }

    }

}
