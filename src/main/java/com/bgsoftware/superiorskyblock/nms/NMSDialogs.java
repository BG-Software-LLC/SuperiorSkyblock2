package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.dialog.DialogWrapper;
import com.bgsoftware.superiorskyblock.listener.BukkitEventsListener;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import org.bukkit.event.Event;

public interface NMSDialogs {

    PlayerCustomClickEventFunctions<?> createCustomClickEventFunctions();

    <V extends MenuView<V, ?>> DialogWrapper<V> createDialog(V menuView);

    void openDialog(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog);

    void closeDialog(SuperiorPlayer superiorPlayer, DialogWrapper<?> dialog);

    interface PlayerCustomClickEventFunctions<E extends Event> extends
            BukkitEventsListener.GameEventCreator<GameEventArgs.DialogClickEvent, E> {

        Class<E> getEventClass();

    }

}
