package com.bgsoftware.superiorskyblock.core.menu.dialog;

import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

public class DialogWrapper<V extends MenuView<V, ?>> {

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

}
