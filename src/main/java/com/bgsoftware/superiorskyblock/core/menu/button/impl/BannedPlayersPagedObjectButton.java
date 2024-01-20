package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandBannedPlayers;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BannedPlayersPagedObjectButton extends AbstractPagedMenuButton<MenuIslandBannedPlayers.View, SuperiorPlayer> {

    private BannedPlayersPagedObjectButton(MenuTemplateButton<MenuIslandBannedPlayers.View> templateButton, MenuIslandBannedPlayers.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), "unban", pagedObject.getName());
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", pagedObject.getName())
                .replaceAll("{1}", pagedObject.getPlayerRole() + "")
                .asSkullOf(pagedObject)
                .build(pagedObject);
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuIslandBannedPlayers.View, SuperiorPlayer> {

        @Override
        public PagedMenuTemplateButton<MenuIslandBannedPlayers.View, SuperiorPlayer> build() {
            return new PagedMenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getButtonIndex(), BannedPlayersPagedObjectButton.class,
                    BannedPlayersPagedObjectButton::new);
        }

    }

}
