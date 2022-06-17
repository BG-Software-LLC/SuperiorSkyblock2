package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class NextPageButton<M extends PagedSuperiorMenu<M, T>, T> extends SuperiorMenuButton<M> {

    private NextPageButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                           String requiredPermission, GameSound lackPermissionSound) {
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
