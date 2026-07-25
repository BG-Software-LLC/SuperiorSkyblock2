package com.bgsoftware.superiorskyblock.core.menu.layout.order;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

import java.util.Iterator;

public interface PagedLayoutOrder<T extends MenuView<T, ?>> {

    int getObjectsPerPageCount();

    MenuButtonsIterator<T> createIterator(MenuTemplateButton<T>[] buttons);

    interface MenuButtonsIterator<T extends MenuView<T, ?>> extends Iterator<MenuTemplateButton<T>> {

        int getSlot();

    }

}
