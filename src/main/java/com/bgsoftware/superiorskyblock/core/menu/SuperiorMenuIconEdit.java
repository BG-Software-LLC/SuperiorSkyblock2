package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import org.jetbrains.annotations.Nullable;

public abstract class SuperiorMenuIconEdit<M extends ISuperiorMenu, T> extends SuperiorMenu<M> {

    protected final T iconProvider;
    protected final TemplateItem iconTemplate;

    protected SuperiorMenuIconEdit(@Nullable SuperiorMenuPattern<M> menuPattern, SuperiorPlayer inventoryViewer,
                                   T iconProvider, TemplateItem iconTemplate) {
        super(menuPattern, inventoryViewer);
        this.iconProvider = iconProvider;
        this.iconTemplate = iconTemplate;
    }

    public T getIconProvider() {
        return iconProvider;
    }

    public TemplateItem getIconTemplate() {
        return iconTemplate;
    }

}
