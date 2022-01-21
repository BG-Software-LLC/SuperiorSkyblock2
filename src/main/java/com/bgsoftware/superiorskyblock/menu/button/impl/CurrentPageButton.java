package com.bgsoftware.superiorskyblock.menu.button.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class CurrentPageButton<M extends PagedSuperiorMenu<M, T>, T> extends SuperiorMenuButton<M> {

    private CurrentPageButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                              String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, M superiorMenu, InventoryClickEvent clickEvent) {
        // Do nothing.
    }

    public static class Builder<M extends PagedSuperiorMenu<M, T>, T> extends
            AbstractBuilder<Builder<M, T>, CurrentPageButton<M, T>, M> {

        @Override
        public CurrentPageButton<M, T> build() {
            return new CurrentPageButton<>(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        }

    }

}
