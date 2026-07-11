package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;

public class NextPageButton<V extends PagedMenuView<V, ?, E>, E> extends AbstractMenuViewButton<V> {

    private NextPageButton(AbstractMenuTemplateButton<V> templateButton, V menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<V> context) {
        PagedMenuLayout<V> pagedMenuPattern = (PagedMenuLayout<V>) menuView.getMenu().getLayout();

        if (pagedMenuPattern == null)
            return;

        int pageObjectSlotsAmount = pagedMenuPattern.getObjectsPerPageCount();
        int currentPage = menuView.getCurrentPage();
        int pagedObjectAmounts = menuView.getPagedObjects().size();

        if (pageObjectSlotsAmount * currentPage < pagedObjectAmounts)
            menuView.setCurrentPage(currentPage + 1);
    }

    public static class Builder<V extends PagedMenuView<V, ?, E>, E> extends AbstractMenuTemplateButton.AbstractBuilder<V> {

        @Override
        public MenuTemplateButton<V> build() {
            return new MenuTemplateButtonImpl<>(this, NextPageButton.class, NextPageButton::new);
        }

    }

}
