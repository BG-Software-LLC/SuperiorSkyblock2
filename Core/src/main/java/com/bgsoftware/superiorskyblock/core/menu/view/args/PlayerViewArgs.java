package com.bgsoftware.superiorskyblock.core.menu.view.args;

import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public class PlayerViewArgs implements ViewArgs {

    private final SuperiorPlayer superiorPlayer;

    public PlayerViewArgs(SuperiorPlayer superiorPlayer) {
        this.superiorPlayer = superiorPlayer;
    }

    public SuperiorPlayer getSuperiorPlayer() {
        return superiorPlayer;
    }

}
