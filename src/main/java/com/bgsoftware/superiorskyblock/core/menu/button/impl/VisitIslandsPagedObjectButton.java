package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuVisitIslands;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class VisitIslandsPagedObjectButton extends AbstractPagedMenuButton<MenuVisitIslands.View, Island> {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private VisitIslandsPagedObjectButton(MenuTemplateButton<MenuVisitIslands.View> templateButton, MenuVisitIslands.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuVisitIslands.View> context) {
        menuView.setPreviousMove(false);

        String args = pagedObject.getOwner().getName();
        if (!BuiltinModules.VISIT.getConfiguration().isOnlyDefaultDimension()) {
            args = args + " " + menuView.getDimension().getName();
        }

        plugin.getCommands().dispatchSubCommand(menuView.getInventoryViewer().asPlayer(), "visit", args);

        BukkitExecutor.sync(menuView::closeView, 1L);
    }

    @Override
    public ItemStack modifyViewItem(ItemBuilder itemBuilder) {
        String ownerName = pagedObject.getOwner().getName();
        String islandName = pagedObject.getName().isEmpty() ? ownerName : pagedObject.getName();

        Locale locale = menuView.getInventoryViewer().getUserLocale();
        String[] description;

        if (!pagedObject.getDescription().isEmpty()) {
            description = pagedObject.getDescription().split("\n");
        } else if (!Message.ISLAND_DESCRIPTION_NONE.isEmpty(locale)) {
            description = new String[]{Message.ISLAND_DESCRIPTION_NONE.getMessage(locale)};
        } else {
            description = EMPTY_STRING_ARRAY;
        }

        return itemBuilder
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

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuVisitIslands.View, Island> {

        @Override
        public PagedMenuTemplateButton<MenuVisitIslands.View, Island> build() {
            return new PagedMenuTemplateButtonImpl<>(this, VisitIslandsPagedObjectButton.class,
                    VisitIslandsPagedObjectButton::new);
        }

    }

}
