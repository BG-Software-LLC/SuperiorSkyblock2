package com.bgsoftware.superiorskyblock.core.menu.view;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;

public abstract class AbstractPagedMenuView<V extends MenuView<V, A>, A extends ViewArgs, E>
        extends AbstractMenuView<V, A> implements PagedMenuView<V, A, E> {

    private List<E> objects;
    private int currentPage = 1;

    public AbstractPagedMenuView(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView, Menu<V, A> menu) {
        super(inventoryViewer, previousMenuView, menu);
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
        return currentPage;
    }

    @Override
    public List<E> getPagedObjects() {
        if (this.objects == null)
            updatePagedObjects();

        return Collections.unmodifiableList(this.objects);
    }

    @Override
    public void updatePagedObjects() {
        this.objects = requestObjects();
    }

    @Override
    public void refreshView() {
        updatePagedObjects();
        super.refreshView();
    }

    protected abstract List<E> requestObjects();

}
