package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;

public class PreviousPageButton<V extends PagedMenuView<V, ?, E>, E> extends AbstractMenuViewButton<V> {

    private PreviousPageButton(AbstractMenuTemplateButton<V> templateButton, V menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<V> context) {
        int newPage = menuView.getCurrentPage() - 1;
        if (newPage >= 1)
            menuView.setCurrentPage(newPage);
    }

    public static class Builder<V extends PagedMenuView<V, ?, E>, E> extends AbstractMenuTemplateButton.AbstractBuilder<V> {

        @Override
        public MenuTemplateButton<V> build() {
            return new MenuTemplateButtonImpl<>(this, PreviousPageButton.class,
                    PreviousPageButton::new);
        }

    }

}
