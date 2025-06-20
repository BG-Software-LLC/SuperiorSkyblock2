package com.bgsoftware.superiorskyblock.core.menu.view.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.IPlayerMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.PlayerViewArgs;

public class PlayerMenuView extends AbstractMenuView<PlayerMenuView, PlayerViewArgs> implements IPlayerMenuView {

    private final SuperiorPlayer superiorPlayer;

    public PlayerMenuView(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
                          Menu<PlayerMenuView, PlayerViewArgs> menu, PlayerViewArgs args) {
        super(inventoryViewer, previousMenuView, menu);
        this.superiorPlayer = args.getSuperiorPlayer();
    }

    @Override
    public SuperiorPlayer getSuperiorPlayer() {
        return this.superiorPlayer;
    }

    @Override
    public String replaceTitle(String title) {
        return title.replace("{}", this.superiorPlayer.getName());
    }

}
