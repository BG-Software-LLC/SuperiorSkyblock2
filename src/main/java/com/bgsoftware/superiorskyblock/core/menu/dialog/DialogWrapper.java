package com.bgsoftware.superiorskyblock.core.menu.dialog;

import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;

public class DialogWrapper<V extends MenuView<V, ?>> {

    private final Int2ObjectMapView<Runnable> callbacks = CollectionsFactory.createInt2ObjectArrayMap();
    private int callbackId = 0;

    private final V menuView;

    private Object handle;

    public DialogWrapper(V menuView) {
        this.menuView = menuView;
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

    public int registerCallback(Runnable callback) {
        int id = callbackId++;
        callbacks.put(id, callback);
        return id;
    }

}
