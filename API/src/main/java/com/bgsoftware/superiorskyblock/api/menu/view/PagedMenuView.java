package com.bgsoftware.superiorskyblock.api.menu.view;

import java.util.List;

/**
 * Similar to {@link MenuView}, but used for views of page-based menus.
 * See {@link MenuView}
 */
public interface PagedMenuView<V extends MenuView<V, A>, A extends ViewArgs, E> extends MenuView<V, A> {

    /**
     * Set the current page index for the paged menu.
     * If the page was changed by this method, the view will be updated, similar to a call to {@link #refreshView()}
     *
     * @param currentPage The new page to show.
     */
    void setCurrentPage(int currentPage);

    /**
     * Get the current page index of the paged menu.
     */
    int getCurrentPage();

    /**
     * Get all the paged objects of the menu.
     */
    List<E> getPagedObjects();

    /**
     * Update the paged objects for the menu.
     * This does not update the actual view. To do that, call {@link #refreshView()}
     */
    void updatePagedObjects();

}
