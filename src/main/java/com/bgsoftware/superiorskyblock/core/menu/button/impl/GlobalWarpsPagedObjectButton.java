package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuGlobalWarps;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Map;

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
        final SuperiorPlayer viewer = menuView.getInventoryViewer();
        final SuperiorPlayer owner = pagedObject.getOwner();
        final String ownerName = owner != null ? owner.getName() : "Unknown";
        String islandName = pagedObject.getName();
        if (islandName == null || islandName.isEmpty()) islandName = ownerName;

        final int warpsCount = pagedObject.getIslandWarps().size();

        Locale userLocale = viewer.getUserLocale();
        String levelNum = Formatters.NUMBER_FORMATTER.format(pagedObject.getIslandLevel());
        String worthNum = Formatters.NUMBER_FORMATTER.format(pagedObject.getWorth());
        String levelFancy = Formatters.FANCY_NUMBER_FORMATTER.format(pagedObject.getIslandLevel(), userLocale);
        String worthFancy = Formatters.FANCY_NUMBER_FORMATTER.format(pagedObject.getWorth(), userLocale);

        int ratingCount = pagedObject.getRatingAmount();

        String ratingAvgNum = Formatters.NUMBER_FORMATTER.format(pagedObject.getTotalRating());
        String ratingAvgFancy = Formatters.RATING_FORMATTER.format(pagedObject.getTotalRating(), userLocale);

        String[] descLines = (pagedObject.getDescription() == null || pagedObject.getDescription().isEmpty())
                ? EMPTY_STRING_ARRAY
                : pagedObject.getDescription().split("\n");

        return new ItemBuilder(buttonItem)
                .asSkullOf(pagedObject.getOwner())
                .replaceAll("{0}", ownerName)
                .replaceLoreWithLines("{1}", descLines)
                .replaceAll("{2}", String.valueOf(warpsCount))
                .replaceAll("{3}", islandName)
                .replaceAll("{4}", levelNum)
                .replaceAll("{5}", levelFancy)
                .replaceAll("{6}", worthNum)
                .replaceAll("{7}", worthFancy)
                .replaceAll("{8}", ratingAvgNum)
                .replaceAll("{9}", ratingAvgFancy)
                .replaceAll("{10}", String.valueOf(ratingCount))
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
