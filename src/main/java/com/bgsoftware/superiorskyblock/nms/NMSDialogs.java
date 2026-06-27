package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.dialog.DialogWrapper;
import org.bukkit.entity.Player;

import java.util.function.IntConsumer;

public interface NMSDialogs {

    boolean isSupported();

    <V extends MenuView<V, ?>> DialogWrapper<V> createDialog(V menuView);

    void openDialog(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog);

    void closeDialog(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog);

}
