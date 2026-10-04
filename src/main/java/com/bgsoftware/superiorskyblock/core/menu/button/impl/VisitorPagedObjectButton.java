package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandVisitors;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class VisitorPagedObjectButton extends AbstractPagedMenuButton<MenuIslandVisitors.View, SuperiorPlayer> {

    private VisitorPagedObjectButton(MenuTemplateButton<MenuIslandVisitors.View> templateButton, MenuIslandVisitors.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuIslandVisitors.View> context) {
        String subCommandToExecute;

        if (context.getClickType().isRightClick())
            subCommandToExecute = "invite";
        else if (context.getClickType().isLeftClick())
            subCommandToExecute = "expel";
        else return;

        plugin.getCommands().dispatchSubCommand(context.getPlayer(), subCommandToExecute, pagedObject.getName());
    }

    @Override
    public ItemStack modifyViewItem(ItemBuilder itemBuilder) {
        Island island = pagedObject.getIsland();
        Locale locale = menuView.getInventoryViewer().getUserLocale();

        String islandOwner = island != null ? island.getOwner().getName() : Message.ISLAND_OWNER_NONE.getMessage(locale);
        String islandName = island != null ? island.getName().isEmpty() ? islandOwner : island.getName() : Message.ISLAND_NAME_NONE.getMessage(locale);

        return itemBuilder
                .replaceAll("{0}", pagedObject.getName())
                .replaceAll("{1}", islandOwner)
                .replaceAll("{2}", islandName)
                .asSkullOf(pagedObject)
                .build(pagedObject);
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuIslandVisitors.View, SuperiorPlayer> {

        @Override
        public PagedMenuTemplateButton<MenuIslandVisitors.View, SuperiorPlayer> build() {
            return new PagedMenuTemplateButtonImpl<>(this, VisitorPagedObjectButton.class,
                    VisitorPagedObjectButton::new);
        }

    }

}
