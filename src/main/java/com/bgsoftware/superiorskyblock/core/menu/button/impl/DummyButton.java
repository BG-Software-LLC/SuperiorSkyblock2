package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class DummyButton<M extends ISuperiorMenu> extends SuperiorMenuButton<M> {

    @SuppressWarnings("rawtypes")
    public static final DummyButton EMPTY_BUTTON = new Builder().build();

    private DummyButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                        String requiredPermission, GameSound lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, ISuperiorMenu superiorMenu, InventoryClickEvent clickEvent) {
        // Dummy button, doesn't do anything when clicked.
    }

    public static class Builder<M extends ISuperiorMenu> extends AbstractBuilder<Builder<M>, DummyButton<M>, M> {

        @Override
        public DummyButton<M> build() {
            return new DummyButton<>(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        }

    }

}
