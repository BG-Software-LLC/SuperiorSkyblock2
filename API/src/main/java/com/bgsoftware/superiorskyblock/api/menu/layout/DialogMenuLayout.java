package com.bgsoftware.superiorskyblock.api.menu.layout;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogBodyElement;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogMenuType;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

import java.util.List;

/**
 * The layout class is used to describe the layout of buttons for a dialog menu.
 * It is later used by the plugin to create a new dialog for the menu.
 */
public interface DialogMenuLayout<V extends MenuView<V, ?>> extends MenuLayout<V> {

    /**
     * Get the type of the dialog menu.
     */
    DialogMenuType getDialogType();

    /**
     * Get whether players can close the dialog menu with the escape button in the keyboard.
     * Closing a dialog menu with escape acts the same as clicking the "Cancel" or "No" buttons.
     */
    boolean isCloseWithEscapeAllowed();

    /**
     * Get the elements in the body of the dialog.
     */
    List<DialogBodyElement> getBodyElements();

    /**
     * Create a new {@link Builder} object for a new {@link DialogMenuLayout}.
     */
    static <V extends MenuView<V, ?>> Builder<V> newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createDialogLayoutBuilder();
    }

    interface Builder<V extends MenuView<V, ?>> extends MenuLayout.Builder<V> {

        /**
         * Set the inventory type for menu views created by this layout.
         *
         * @param inventoryType The inventory type to set.
         */
        Builder<V> setDialogType(DialogMenuType inventoryType);

        /**
         * Set whether it is allowed to close the dialog with escape.
         *
         * @param closeWithEscapeAllowed Whether it is allowed
         */
        Builder<V> setCloseWithEscapeAllowed(boolean closeWithEscapeAllowed);

        /**
         * Add a {@link DialogBodyElement} to the body of this dialog.
         *
         * @param bodyElement The body element to add.
         */
        Builder<V> addBodyElement(DialogBodyElement bodyElement);

        /**
         * Get the {@link DialogMenuLayout} from this builder.
         */
        DialogMenuLayout<V> build();

    }

}
