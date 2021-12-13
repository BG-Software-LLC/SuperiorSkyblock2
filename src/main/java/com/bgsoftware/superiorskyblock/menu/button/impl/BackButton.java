package com.bgsoftware.superiorskyblock.menu.button.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class BackButton<M extends ISuperiorMenu> extends SuperiorMenuButton<M> {

    private BackButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                       String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, M superiorMenu, InventoryClickEvent clickEvent) {

    }

    public static class Builder<M extends ISuperiorMenu> extends AbstractBuilder<Builder<M>, BackButton<M>, M> {

        @Override
        public BackButton<M> build() {
            return new BackButton<>(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        }

    }

}
