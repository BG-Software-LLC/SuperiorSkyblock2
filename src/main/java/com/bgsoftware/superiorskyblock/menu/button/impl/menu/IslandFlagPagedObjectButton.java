package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.formatting.Formatters;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandFlags;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class IslandFlagPagedObjectButton extends PagedObjectButton<MenuIslandFlags, MenuIslandFlags.IslandFlagInfo> {

    private IslandFlagPagedObjectButton(TemplateItem buttonItem, List<String> commands,
                                        String requiredPermission, SoundWrapper lackPermissionSound,
                                        TemplateItem nullItem, int objectIndex) {
        super(buttonItem, null, commands, requiredPermission, lackPermissionSound, nullItem, objectIndex);
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

        SoundWrapper clickSound = pagedObject.getClickSound();
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
