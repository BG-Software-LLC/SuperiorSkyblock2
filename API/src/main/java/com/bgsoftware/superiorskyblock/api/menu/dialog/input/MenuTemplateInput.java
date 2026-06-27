package com.bgsoftware.superiorskyblock.api.menu.dialog.input;

import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

public interface MenuTemplateInput<V extends MenuView<V, ?>> {

    /**
     * Get the input's slot in the menu.
     */
    int getSlot();

    String getLabel();

    interface Builder<V extends MenuView<V, ?>> {

        /**
         * Set the slot of the input in the menu.
         *
         * @param slot The slot of the input.
         */
        Builder<V> setSlot(int slot);

    }

}
