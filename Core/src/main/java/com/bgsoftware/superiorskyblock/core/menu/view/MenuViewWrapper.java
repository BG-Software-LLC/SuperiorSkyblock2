package com.bgsoftware.superiorskyblock.core.menu.view;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.inventory.Inventory;

public class MenuViewWrapper implements ISuperiorMenu {

    private final MenuView<?, ?> menuView;

    @SuppressWarnings("deprecation")
    public static ISuperiorMenu fromView(@Nullable MenuView<?, ?> menuView) {
        return menuView == null || menuView instanceof ISuperiorMenu ? (ISuperiorMenu) menuView : new MenuViewWrapper(menuView);
    }

    private MenuViewWrapper(MenuView<?, ?> menuView) {
        this.menuView = menuView;
    }

    @Override
    public void cloneAndOpen(@Nullable ISuperiorMenu previousMenu) {
        this.menuView.setPreviousMenuView(previousMenu, false);
        this.menuView.refreshView();
    }

    @Nullable
    @Override
    public ISuperiorMenu getPreviousMenu() {
        return new MenuViewWrapper(menuView.getPreviousMenuView());
    }

    @Override
    public SuperiorPlayer getInventoryViewer() {
        return menuView.getInventoryViewer();
    }

    @Nullable
    @Override
    public MenuView<?, ?> getPreviousMenuView() {
        return menuView.getPreviousMenuView();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setPreviousMenuView(@Nullable MenuView previousMenuView, boolean keepOlderViews) {
        menuView.setPreviousMenuView(previousMenuView, keepOlderViews);
    }

    @Override
    public Menu<?, ?> getMenu() {
        return menuView.getMenu();
    }

    @Override
    public void setPreviousMove(boolean previousMove) {
        menuView.setPreviousMove(previousMove);
    }

    @Override
    public boolean isPreviousMenu() {
        return menuView.isPreviousMenu();
    }

    @Override
    public Inventory getInventory() {
        return menuView.getInventory();
    }

    @Override
    public void refreshView() {
        this.menuView.refreshView();
    }

    @Override
    public void closeView() {
        this.menuView.closeView();
    }

}
