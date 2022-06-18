package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class CurrentPageButton<M extends PagedSuperiorMenu<M, T>, T> extends SuperiorMenuButton<M> {

    private CurrentPageButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                              String requiredPermission, GameSound lackPermissionSound) {
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
