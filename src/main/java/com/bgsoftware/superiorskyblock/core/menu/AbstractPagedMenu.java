package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.superiorskyblock.api.menu.PagedMenu;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public abstract class AbstractPagedMenu<V extends AbstractPagedMenuView<V, A, E>, A extends ViewArgs, E>
        extends AbstractMenu<V, A> implements PagedMenu<V, A, E> {

    private final boolean acceptNull;

    protected AbstractPagedMenu(String identifier, MenuParseResult<V> parseResult, boolean acceptNull) {
        super(identifier, parseResult);
        Preconditions.checkState(parseResult.getLayoutBuilder() instanceof PagedMenuLayout.Builder, "Paged menu " + identifier + " doesn't use the correct layout.");
        this.acceptNull = acceptNull;
    }

    @Override
    public boolean onPreButtonClick(MenuViewButton<V> menuButton, InventoryClickEvent clickEvent) {
        if (!(menuButton instanceof PagedMenuViewButton))
            return true;

        MenuLayout<V> menuLayout = getLayout();

        if (!(menuLayout instanceof PagedMenuLayout))
            return false;

        PagedMenuViewButton<V, E> pagedMenuButton = (PagedMenuViewButton<V, E>) menuButton;

        V menuView = menuButton.getView();

        menuView.updatePagedObjects();
        List<E> pagedObjects = menuView.getPagedObjects();

        int objectsPerPage = ((PagedMenuLayout<V>) menuLayout).getObjectsPerPageCount();

        int currentPage = menuView.getCurrentPage();

        int objectIndex = ((PagedMenuTemplateButton<V, E>) pagedMenuButton.getTemplate()).getButtonIndex() + (objectsPerPage * (currentPage - 1));

        if (objectIndex >= pagedObjects.size()) {
            if (this.acceptNull)
                pagedMenuButton.updateObject(null);
            return this.acceptNull;
        }

        pagedMenuButton.updateObject(pagedObjects.get(objectIndex));

        return true;
    }

}
