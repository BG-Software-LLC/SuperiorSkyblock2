package com.bgsoftware.superiorskyblock.menu.button.impl;

import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class NextPageButton extends SuperiorMenuButton {

    private NextPageButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                           String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorMenu superiorMenu, InventoryClickEvent clickEvent) {
        Preconditions.checkArgument(superiorMenu instanceof PagedSuperiorMenu, "superiorMenu must be a PagedSuperiorMenu.");
        Preconditions.checkNotNull(superiorMenu.getMenuPattern(), "superiorMenu must be initialized.");

        PagedSuperiorMenu<?> pagedSuperiorMenu = (PagedSuperiorMenu<?>) superiorMenu;
        PagedMenuPattern<?> pagedMenuPattern = (PagedMenuPattern<?>) superiorMenu.getMenuPattern();

        int pageObjectSlotsAmount = pagedMenuPattern.getObjectsPerPage();
        int currentPage = pagedSuperiorMenu.getCurrentPage();
        int pagedObjectAmounts = pagedSuperiorMenu.getPagedObjects().size();

        if (pageObjectSlotsAmount * currentPage < pagedObjectAmounts)
            pagedSuperiorMenu.movePage(currentPage + 1);
    }

    public static class Builder extends AbstractBuilder<Builder, NextPageButton> {

        @Override
        public NextPageButton build() {
            return new NextPageButton(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        }

    }

}
