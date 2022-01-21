package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import org.jetbrains.annotations.Nullable;

public abstract class SuperiorMenuIconEdit<M extends ISuperiorMenu, T> extends SuperiorMenu<M> {

    protected final T iconProvider;
    protected final ItemBuilder iconBuilder;

    protected SuperiorMenuIconEdit(@Nullable SuperiorMenuPattern<M> menuPattern, SuperiorPlayer inventoryViewer,
                                   T iconProvider, ItemBuilder iconBuilder) {
        super(menuPattern, inventoryViewer);
        this.iconProvider = iconProvider;
        this.iconBuilder = iconBuilder;
    }

    public T getIconProvider() {
        return iconProvider;
    }

    public ItemBuilder getIconBuilder() {
        return iconBuilder;
    }

}
