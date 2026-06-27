package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.dialog.DialogWrapper;
import org.bukkit.entity.Player;

public class NMSDialogsFallback implements NMSDialogs {

    @Override
    public boolean isSupported() {
        return false;
    }

    @Override
    public <V extends MenuView<V, ?>> DialogWrapper<V> createDialog(V menuView) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void openDialog(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void closeDialog(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog) {
        throw new UnsupportedOperationException("Not supported");
    }

}
