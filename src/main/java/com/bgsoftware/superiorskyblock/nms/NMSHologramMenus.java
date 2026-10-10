package com.bgsoftware.superiorskyblock.nms;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface NMSHologramMenus {

    /**
     * Create a new renderer of a hologram menu.
     * The menu is rendered on a flat panel that is located {@code distance} blocks in front of {@code origin}.
     * Elements are positioned on the panel by their center, with the x-axis pointing to the right of the viewer
     * and the y-axis pointing up. Elements that are added later are rendered in front of earlier ones.
     *
     * @param viewer   The player to render the menu to.
     * @param origin   The location of the eyes of the viewer, looking at the center of the panel.
     * @param distance The distance between the viewer and the panel.
     * @param listener Listener to inputs the viewer does while the menu is shown.
     */
    Renderer createRenderer(Player viewer, Location origin, float distance, InputListener listener);

    interface Renderer {

        int addText(String text, float x, float y, float scale, boolean shadowed);

        int addRectangle(float x, float y, float width, float height, int argb);

        /**
         * Add an item that is rendered flat, the same way it is rendered in inventories.
         */
        int addItem(ItemStack itemStack, float x, float y, float width, float height);

        /**
         * Add a text that has a background and can have multiple lines.
         * Unlike other elements, it is positioned by the center of its bottom edge.
         */
        int addTooltip(String text, float x, float y, float scale, int argb);

        /**
         * Show all the elements that were added and were not shown yet.
         * On first call, the view of the viewer is locked on the panel and his inputs are captured.
         */
        void show();

        void moveElement(int elementId, float x, float y);

        /**
         * Set the color of the background of a rectangle or a tooltip.
         */
        void setColor(int elementId, int argb);

        /**
         * Set the text of a text or a tooltip.
         */
        void setText(int elementId, String text);

        /**
         * Remove all the elements, without unlocking the view of the viewer.
         */
        void clear();

        /**
         * Remove all the elements and give the viewer back the control over his view.
         */
        void destroy();

    }

    interface InputListener {

        void onClick(boolean rightClick);

        void onExit();

    }

}
