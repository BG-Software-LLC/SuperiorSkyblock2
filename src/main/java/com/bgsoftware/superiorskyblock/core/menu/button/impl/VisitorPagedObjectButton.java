package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandVisitors;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class VisitorPagedObjectButton extends AbstractPagedMenuButton<MenuIslandVisitors.View, SuperiorPlayer> {

    private VisitorPagedObjectButton(MenuTemplateButton<MenuIslandVisitors.View> templateButton, MenuIslandVisitors.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        String subCommandToExecute;

        if (clickEvent.getClick().isRightClick())
            subCommandToExecute = "invite";
        else if (clickEvent.getClick().isLeftClick())
            subCommandToExecute = "expel";
        else return;

        plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), subCommandToExecute, pagedObject.getName());
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        Island island = pagedObject.getIsland();

        String islandOwner = island != null ? island.getOwner().getName() : "None";
        String islandName = island != null ? island.getName().isEmpty() ? islandOwner : island.getName() : "None";

        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", pagedObject.getName())
                .replaceAll("{1}", islandOwner)
                .replaceAll("{2}", islandName)
                .asSkullOf(pagedObject)
                .build(pagedObject);
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuIslandVisitors.View, SuperiorPlayer> {

        @Override
        public PagedMenuTemplateButton<MenuIslandVisitors.View, SuperiorPlayer> build() {
            return new PagedMenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getButtonIndex(), VisitorPagedObjectButton.class,
                    VisitorPagedObjectButton::new);
        }

    }

}
