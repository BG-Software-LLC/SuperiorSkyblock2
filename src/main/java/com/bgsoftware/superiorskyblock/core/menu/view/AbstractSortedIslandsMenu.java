package com.bgsoftware.superiorskyblock.core.menu.view;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.HashSet;
import java.util.Set;

public class AbstractSortedIslandsMenu {

    public static class Args implements ViewArgs {

        protected final SortingType sortingType;
        protected final String selectedSortingType;
        protected final String unselectedSortingType;

        public Args(SortingType sortingType, String selectedSortingType, String unselectedSortingType) {
            this.sortingType = sortingType;
            this.selectedSortingType = selectedSortingType;
            this.unselectedSortingType = unselectedSortingType;
        }

    }

    public abstract static class View<V extends View<V, A>, A extends Args>
            extends AbstractPagedMenuView<V, A, Island> {

        private final Set<SortingType> alreadySorted = new HashSet<>();
        private SortingType sortingType;
        private final String selectedSortingType;
        private final String unselectedSortingType;

        protected View(SuperiorPlayer inventoryViewer, MenuView<?, ?> previousMenuView, Menu<V, A> menu, A args) {
            super(inventoryViewer, previousMenuView, menu);
            this.sortingType = args.sortingType;
            this.selectedSortingType = args.selectedSortingType;
            this.unselectedSortingType = args.unselectedSortingType;
        }

        public SortingType getSortingType() {
            return sortingType;
        }

        public String getSelectedSortingType() {
            return selectedSortingType;
        }

        public String getUnselectedSortingType() {
            return unselectedSortingType;
        }

        public boolean setSortingType(SortingType sortingType) {
            this.sortingType = sortingType;
            this.updatePagedObjects();
            return alreadySorted.add(sortingType);
        }

    }

}
