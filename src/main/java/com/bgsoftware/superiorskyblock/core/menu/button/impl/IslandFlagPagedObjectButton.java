package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandFlags;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class IslandFlagPagedObjectButton extends AbstractPagedMenuButton<MenuIslandFlags.View, MenuIslandFlags.IslandFlagInfo> {

    private IslandFlagPagedObjectButton(MenuTemplateButton<MenuIslandFlags.View> templateButton, MenuIslandFlags.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();

        Island island = menuView.getIsland();

        IslandFlag islandFlag = pagedObject.getIslandFlag();

        if (islandFlag == null)
            return;

        if (island.hasSettingsEnabled(islandFlag)) {
            if (!plugin.getEventsBus().callIslandDisableFlagEvent(inventoryViewer, island, islandFlag))
                return;

            island.disableSettings(islandFlag);
        } else {
            if (!plugin.getEventsBus().callIslandEnableFlagEvent(inventoryViewer, island, islandFlag))
                return;

            island.enableSettings(islandFlag);
        }

        GameSoundImpl.playSound(clickEvent.getWhoClicked(), pagedObject.getClickSound());

        Message.UPDATED_SETTINGS.send(inventoryViewer, Formatters.CAPITALIZED_FORMATTER.format(islandFlag.getName()));

        menuView.refreshView();
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        Island island = menuView.getIsland();

        IslandFlag islandFlag = pagedObject.getIslandFlag();

        return islandFlag != null && island.hasSettingsEnabled(islandFlag) ?
                pagedObject.getEnabledIslandFlagItem().build(inventoryViewer) :
                pagedObject.getDisabledIslandFlagItem().build(inventoryViewer);
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuIslandFlags.View, MenuIslandFlags.IslandFlagInfo> {

        @Override
        public PagedMenuTemplateButton<MenuIslandFlags.View, MenuIslandFlags.IslandFlagInfo> build() {
            return new PagedMenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getButtonIndex(), IslandFlagPagedObjectButton.class,
                    IslandFlagPagedObjectButton::new);
        }

    }

}
