package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandUniqueVisitors;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.Locale;

public class UniqueVisitorPagedObjectButton extends AbstractPagedMenuButton<MenuIslandUniqueVisitors.View, MenuIslandUniqueVisitors.UniqueVisitorInfo> {

    private UniqueVisitorPagedObjectButton(MenuTemplateButton<MenuIslandUniqueVisitors.View> templateButton, MenuIslandUniqueVisitors.View menuView) {
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

        plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(),
                subCommandToExecute, pagedObject.getVisitor().getName());
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        SuperiorPlayer visitor = pagedObject.getVisitor();
        Island island = visitor.getIsland();
        Locale locale = menuView.getInventoryViewer().getUserLocale();

        String islandOwner = island != null ? island.getOwner().getName() : Message.ISLAND_OWNER_NONE.getMessage(locale);
        String islandName = island != null ? island.getName().isEmpty() ? islandOwner : island.getName() : Message.ISLAND_NAME_NONE.getMessage(locale);

        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", visitor.getName())
                .replaceAll("{1}", islandOwner)
                .replaceAll("{2}", islandName)
                .replaceAll("{3}", Formatters.DATE_FORMATTER.format(new Date(pagedObject.getVisitTime())))
                .asSkullOf(visitor)
                .build(visitor);
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuIslandUniqueVisitors.View, MenuIslandUniqueVisitors.UniqueVisitorInfo> {

        @Override
        public PagedMenuTemplateButton<MenuIslandUniqueVisitors.View, MenuIslandUniqueVisitors.UniqueVisitorInfo> build() {
            return new PagedMenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getButtonIndex(), UniqueVisitorPagedObjectButton.class,
                    UniqueVisitorPagedObjectButton::new);
        }

    }

}
