package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandUniqueVisitors;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.Locale;

public class UniqueVisitorPagedObjectButton extends AbstractPagedMenuButton<MenuIslandUniqueVisitors.View, MenuIslandUniqueVisitors.UniqueVisitorInfo> {

    private UniqueVisitorPagedObjectButton(MenuTemplateButton<MenuIslandUniqueVisitors.View> templateButton, MenuIslandUniqueVisitors.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuIslandUniqueVisitors.View> context) {
        String subCommandToExecute;

        if (context.getClickType().isRightClick())
            subCommandToExecute = "invite";
        else if (context.getClickType().isLeftClick())
            subCommandToExecute = "expel";
        else return;

        plugin.getCommands().dispatchSubCommand(context.getPlayer(),
                subCommandToExecute, pagedObject.getVisitor().getName());
    }

    @Override
    public ItemStack modifyViewItem(ItemBuilder itemBuilder) {
        SuperiorPlayer visitor = pagedObject.getVisitor();
        Island island = visitor.getIsland();
        Locale locale = menuView.getInventoryViewer().getUserLocale();

        String islandOwner = island != null ? island.getOwner().getName() : Message.ISLAND_OWNER_NONE.getMessage(locale);
        String islandName = island != null ? island.getName().isEmpty() ? islandOwner : island.getName() : Message.ISLAND_NAME_NONE.getMessage(locale);

        return itemBuilder
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
            return new PagedMenuTemplateButtonImpl<>(this, UniqueVisitorPagedObjectButton.class,
                    UniqueVisitorPagedObjectButton::new);
        }

    }

}
