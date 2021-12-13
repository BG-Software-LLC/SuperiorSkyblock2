package com.bgsoftware.superiorskyblock.menu.button.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class PreviousPageButton<M extends PagedSuperiorMenu<M, T>, T> extends SuperiorMenuButton<M> {

    private PreviousPageButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                               String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, M superiorMenu, InventoryClickEvent clickEvent) {
        int newPage = superiorMenu.getCurrentPage() - 1;
        if (newPage >= 1)
            superiorMenu.movePage(newPage);
    }

    public static class Builder<M extends PagedSuperiorMenu<M, T>, T> extends
            AbstractBuilder<Builder<M, T>, PreviousPageButton<M, T>, M> {

        @Override
        public PreviousPageButton<M, T> build() {
            return new PreviousPageButton<>(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        }

    }

}
