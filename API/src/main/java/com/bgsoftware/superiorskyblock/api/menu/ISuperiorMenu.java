package com.bgsoftware.superiorskyblock.api.menu;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

/**
 * @deprecated See {@link MenuView}
 */
@Deprecated
public interface ISuperiorMenu extends MenuView {

    /**
     * Clone and open this menu to the {@link #getInventoryViewer()}
     *
     * @param previousMenu The previous menu to set.
     */
    void cloneAndOpen(@Nullable ISuperiorMenu previousMenu);

    /**
     * Get the previous menu of this menu.
     */
    @Nullable
    ISuperiorMenu getPreviousMenu();

    /**
     * Helper method to cast the new {@link MenuView} object to the old {@link ISuperiorMenu} object.
     */
    @Deprecated
    static ISuperiorMenu convertFromView(MenuView<?, ?> menuView) {
        return SuperiorSkyblockAPI.getMenus().getOldMenuFromView(menuView);
    }

}
