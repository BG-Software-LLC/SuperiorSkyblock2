package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class BackButton<M extends ISuperiorMenu> extends SuperiorMenuButton<M> {

    private BackButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                       String requiredPermission, GameSound lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, M superiorMenu, InventoryClickEvent clickEvent) {
        // Do nothing.
    }

    public static class Builder<M extends ISuperiorMenu> extends AbstractBuilder<Builder<M>, BackButton<M>, M> {

        @Override
        public BackButton<M> build() {
            return new BackButton<>(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        }

    }

}
