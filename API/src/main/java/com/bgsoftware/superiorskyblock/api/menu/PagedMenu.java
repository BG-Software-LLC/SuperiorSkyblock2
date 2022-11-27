package com.bgsoftware.superiorskyblock.api.menu;

import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;

/**
 * Similar to {@link Menu}, but used for describing page-based menus.
 * See {@link Menu}
 */
public interface PagedMenu<V extends PagedMenuView<V, A, E>, A extends ViewArgs, E> extends Menu<V, A> {

}
