package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuGlobalWarps;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class GlobalWarpsPagedObjectButton extends AbstractPagedMenuButton<MenuGlobalWarps.View, Island> {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private GlobalWarpsPagedObjectButton(MenuTemplateButton<MenuGlobalWarps.View> templateButton, MenuGlobalWarps.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        if (Menus.MENU_GLOBAL_WARPS.isVisitorWarps()) {
            menuView.setPreviousMove(false);
            plugin.getCommands().dispatchSubCommand(menuView.getInventoryViewer().asPlayer(),
                    "visit", pagedObject.getOwner().getName());
        } else {
            plugin.getProviders().getMenusProvider().openWarpCategories(
                    menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), pagedObject);
        }
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        String ownerName = pagedObject.getOwner().getName();
        String islandName = pagedObject.getName().isEmpty() ? ownerName : pagedObject.getName();

        Locale locale = menuView.getInventoryViewer().getUserLocale();
        String[] description;

        if (!pagedObject.getDescription().isEmpty())
            description = pagedObject.getDescription().split("\n");
        else if (!Message.ISLAND_DESCRIPTION_NONE.isEmpty(locale))
            description = new String[] {Message.ISLAND_DESCRIPTION_NONE.getMessage(locale)};
        else
            description = EMPTY_STRING_ARRAY;

        return new ItemBuilder(buttonItem)
                .asSkullOf(pagedObject.getOwner())
                .replaceAll("{0}", ownerName)
                .replaceLoreWithLines("{1}", description)
                .replaceAll("{2}", String.valueOf(pagedObject.getIslandWarps().size()))
                .replaceAll("{3}", islandName)
                .replaceAll("{4}", Formatters.NUMBER_FORMATTER.format(pagedObject.getIslandLevel()))
                .replaceAll("{5}", Formatters.FANCY_NUMBER_FORMATTER.format(pagedObject.getIslandLevel(), locale))
                .replaceAll("{6}", Formatters.NUMBER_FORMATTER.format(pagedObject.getWorth()))
                .replaceAll("{7}", Formatters.FANCY_NUMBER_FORMATTER.format(pagedObject.getWorth(), locale))
                .replaceAll("{8}", Formatters.NUMBER_FORMATTER.format(pagedObject.getTotalRating()))
                .replaceAll("{9}", Formatters.RATING_FORMATTER.format(pagedObject.getTotalRating(), locale))
                .replaceAll("{10}", String.valueOf(pagedObject.getRatingAmount()))
                .build(pagedObject.getOwner());
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuGlobalWarps.View, Island> {

        @Override
        public PagedMenuTemplateButton<MenuGlobalWarps.View, Island> build() {
            return new PagedMenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getButtonIndex(), GlobalWarpsPagedObjectButton.class,
                    GlobalWarpsPagedObjectButton::new);
        }

    }

}
