package com.bgsoftware.superiorskyblock.core.menu.layout.custom;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

import java.util.Iterator;
import java.util.List;

public interface PagedLayout<T extends MenuView<T, ?>> {

    int getObjectsPerPageCount();

    MenuButtonsIterator<T> createIterator(MenuTemplateButton<T>[] buttons);

    interface MenuButtonsIterator<T extends MenuView<T, ?>> extends Iterator<MenuTemplateButton<T>> {

        List<Integer> getSlots();

    }

}
