package com.bgsoftware.superiorskyblock.core.menu.view;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;

public class AbstractIconProviderMenu {

    public static class Args<E> implements ViewArgs {

        private final E iconProvider;
        private final TemplateItem iconTemplate;

        public Args(E iconProvider, TemplateItem iconTemplate) {
            this.iconProvider = iconProvider;
            this.iconTemplate = iconTemplate;
        }

    }

    public abstract static class View<E> extends AbstractMenuView<View<E>, Args<E>> {

        private final E iconProvider;
        private final TemplateItem iconTemplate;

        protected View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
                       Menu<View<E>, Args<E>> menu, Args<E> args) {
            super(inventoryViewer, previousMenuView, menu);
            this.iconProvider = args.iconProvider;
            this.iconTemplate = args.iconTemplate;
        }

        public E getIconProvider() {
            return iconProvider;
        }

        public TemplateItem getIconTemplate() {
            return iconTemplate;
        }

        @Override
        public abstract String replaceTitle(String title);

    }

}
