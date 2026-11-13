package com.bgsoftware.superiorskyblock.api.menu.layout;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramMenuStyle;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

import java.util.List;

/**
 * The layout class is used to describe the layout of buttons for a hologram menu.
 * It is later used by the plugin to create a new hologram for the menu.
 * <p>
 * Hologram menus are rendered in front of the player using display entities that are only visible to him.
 * The buttons of the layout can display their item, a label, or both. Their appearance is described using
 * {@link com.bgsoftware.superiorskyblock.api.menu.hologram.HologramButton}.
 */
public interface HologramMenuLayout<V extends MenuView<V, ?>> extends MenuLayout<V> {

    /**
     * Get the lines of text that are shown between the title and the buttons of the hologram menu.
     */
    List<String> getBodyLines();

    /**
     * Get the appearance of the hologram menu.
     */
    HologramMenuStyle getStyle();

    /**
     * Get the rows of buttons of the hologram menu, from top to bottom.
     * Each row is a list of the slots of its buttons, from left to right.
     * If no rows were added to the layout, each button is in a row of its own.
     */
    List<List<Integer>> getRows();

    /**
     * Create a new {@link Builder} object for a new {@link HologramMenuLayout}.
     */
    static <V extends MenuView<V, ?>> Builder<V> newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createHologramLayoutBuilder();
    }

    interface Builder<V extends MenuView<V, ?>> extends MenuLayout.Builder<V> {

        /**
         * Add a line of text to the body of this hologram menu.
         *
         * @param bodyLine The line to add.
         */
        Builder<V> addBodyLine(String bodyLine);

        /**
         * Set the appearance of this hologram menu.
         *
         * @param style The appearance to set.
         */
        Builder<V> setStyle(HologramMenuStyle style);

        /**
         * Add a row of buttons to this hologram menu, below the rows that were already added.
         * Slots without a button are left as empty space in the row.
         *
         * @param slots The slots of the buttons of the row, from left to right.
         */
        Builder<V> addRow(List<Integer> slots);

        /**
         * Get the {@link HologramMenuLayout} from this builder.
         */
        HologramMenuLayout<V> build();

    }

}
