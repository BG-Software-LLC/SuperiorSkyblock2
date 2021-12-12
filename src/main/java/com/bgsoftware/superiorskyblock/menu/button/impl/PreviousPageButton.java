package com.bgsoftware.superiorskyblock.menu.button.impl;

import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class PreviousPageButton extends SuperiorMenuButton {

    private PreviousPageButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                               String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorMenu superiorMenu, InventoryClickEvent clickEvent) {
        Preconditions.checkArgument(superiorMenu instanceof PagedSuperiorMenu, "superiorMenu must be a PagedSuperiorMenu.");

        PagedSuperiorMenu<?> pagedSuperiorMenu = (PagedSuperiorMenu<?>) superiorMenu;

        int newPage = pagedSuperiorMenu.getCurrentPage() - 1;

        if (newPage >= 1)
            pagedSuperiorMenu.movePage(newPage);
    }

    public static class Builder extends AbstractBuilder<Builder, PreviousPageButton> {

        @Override
        public PreviousPageButton build() {
            return new PreviousPageButton(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        }

    }

}
