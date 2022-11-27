package com.bgsoftware.superiorskyblock.core.menu.view;

import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.view.args.EmptyViewArgs;

import javax.annotation.Nullable;

public class BaseMenuView extends AbstractMenuView<BaseMenuView, EmptyViewArgs> {

    public BaseMenuView(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
                        Menu<BaseMenuView, EmptyViewArgs> menu) {
        super(inventoryViewer, previousMenuView, menu);
    }

}
