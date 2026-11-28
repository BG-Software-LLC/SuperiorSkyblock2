package com.bgsoftware.superiorskyblock.core.menu.dialog;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DialogWrapper<V extends MenuView<V, ?>> {

    private static final Int2ObjectMapView<DialogWrapper<?>> DIALOGS_BY_IDS = CollectionsFactory.createInt2ObjectArrayMap();
    private static final Map<UUID, DialogWrapper<?>> DIALOGS_BY_PLAYERS = new HashMap<>();
    private static final Synchronized<Void> mutex = Synchronized.of(null);
    private static final AtomicInteger dialogIds = new AtomicInteger(0);

    private final int id;
    private final UUID playerUUID;
    private final V menuView;

    private Object handle;

    @Nullable
    public static DialogWrapper<?> getById(int id) {
        return mutex.readAndGet(o -> DIALOGS_BY_IDS.get(id));
    }

    public static DialogWrapper<?> getByPlayer(UUID playerUUID) {
        return mutex.readAndGet(o -> DIALOGS_BY_PLAYERS.get(playerUUID));
    }

    public DialogWrapper(V menuView) {
        this.id = dialogIds.getAndIncrement();
        this.playerUUID = menuView.getInventoryViewer().getUniqueId();
        this.menuView = menuView;
    }

    public int getId() {
        return id;
    }

    public V getMenuView() {
        return menuView;
    }

    public void setHandle(Object handle) {
        this.handle = handle;
    }

    public Object getHandle() {
        return handle;
    }

    public void onOpenDialog() {
        mutex.write(o -> {
            DialogWrapper<?> oldDialog = DIALOGS_BY_PLAYERS.put(this.playerUUID, this);
            if (oldDialog != null)
                oldDialog.onCloseDialog();
            DIALOGS_BY_IDS.put(this.id, this);
        });
    }

    public void onCloseDialog() {
        mutex.write(o -> {
            DIALOGS_BY_IDS.remove(this.id);
            DIALOGS_BY_PLAYERS.remove(this.playerUUID);
        });
    }

}
