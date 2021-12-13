package com.bgsoftware.superiorskyblock.menu.button.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class NextPageButton<M extends PagedSuperiorMenu<M, T>, T> extends SuperiorMenuButton<M> {

    private NextPageButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                           String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, M superiorMenu, InventoryClickEvent clickEvent) {
        PagedMenuPattern<M, T> pagedMenuPattern = (PagedMenuPattern<M, T>) superiorMenu.getMenuPattern();

        if (pagedMenuPattern == null)
            return;

        int pageObjectSlotsAmount = pagedMenuPattern.getObjectsPerPage();
        int currentPage = superiorMenu.getCurrentPage();
        int pagedObjectAmounts = superiorMenu.getPagedObjects().size();

        if (pageObjectSlotsAmount * currentPage < pagedObjectAmounts)
            superiorMenu.movePage(currentPage + 1);
    }

    public static class Builder<M extends PagedSuperiorMenu<M, T>, T> extends
            AbstractBuilder<Builder<M, T>, NextPageButton<M, T>, M> {

        @Override
        public NextPageButton<M, T> build() {
            return new NextPageButton<>(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        }

    }

}
