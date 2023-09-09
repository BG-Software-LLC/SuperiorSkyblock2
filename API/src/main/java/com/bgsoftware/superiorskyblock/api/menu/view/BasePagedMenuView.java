package com.bgsoftware.superiorskyblock.api.menu.view;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;

import java.util.List;

public abstract class BasePagedMenuView<V extends MenuView<V, A>, A extends ViewArgs, E> extends BaseMenuView<V, A> implements PagedMenuView<V, A, E> {

    protected int currentPage = 1;

    protected BasePagedMenuView(SuperiorPlayer inventoryViewer, Menu<V, A> menu, @Nullable MenuView<?, ?> previousMenuView) {
        super(inventoryViewer, menu, previousMenuView);
    }

    @Override
    public void setCurrentPage(int currentPage) {
        Preconditions.checkArgument(currentPage >= 1, "invalid page " + currentPage);

        if (this.currentPage == currentPage)
            return;

        this.currentPage = currentPage;

        setPreviousMove(false);
        refreshView();
    }

    @Override
    public int getCurrentPage() {
        return this.currentPage;
    }

    @Override
    public abstract List<E> getPagedObjects();

    @Override
    public abstract void updatePagedObjects();

}
