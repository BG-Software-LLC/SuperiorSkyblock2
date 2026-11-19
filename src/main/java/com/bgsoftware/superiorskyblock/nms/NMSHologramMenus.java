package com.bgsoftware.superiorskyblock.nms;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public interface NMSHologramMenus {

    /**
     * Create a new renderer of a hologram menu. Called from the main thread.
     * The menu is rendered on a flat panel that is located {@code distance} blocks in front of {@code origin}.
     * Elements are positioned on the panel by their center, with the x-axis pointing to the right of the viewer
     * and the y-axis pointing up. Elements that are added later are rendered in front of earlier ones.
     *
     * @param viewer        The player to render the menu to.
     * @param origin        The location of the eyes of the viewer, looking at the center of the panel.
     * @param distance      The distance between the viewer and the panel.
     * @param hideCrosshair Whether to hide the crosshair of the viewer, by putting him in spectator mode on his side.
     *                      His hand and his hotbar are hidden as well, and only his left clicks are reported.
     * @param listener      Listener to inputs the viewer does while the menu is shown.
     */
    Renderer createRenderer(Player viewer, Location origin, float distance, boolean hideCrosshair, InputListener listener);

    /**
     * Capture the blocks around a location, so they can be read outside the main thread.
     * Called from the main thread. Only loaded chunks are captured.
     *
     * @param center The location to capture the blocks around.
     * @param radius The distance from the location to capture the blocks in.
     */
    CapturedBlocks captureBlocks(Location center, double radius);

    interface CapturedBlocks {

    }

    /**
     * All the methods of renderers are called from a single thread, which is not the main thread.
     * Work that must be done on the main thread is returned to the caller.
     * Identifiers of elements are never reused, so removed elements can be safely referenced.
     */
    interface Renderer {

        /**
         * Get whether the viewer is still connected to the server.
         */
        boolean isViewerConnected();

        /**
         * Called every tick while the menu is shown.
         * The viewer only sends his rotation while he is in the menu, so he is kept from being kicked for flying
         * when the menu was opened in the air.
         */
        void tick();

        /**
         * Get the rotation of the head of the viewer, as last received from him.
         */
        float getViewerYaw();

        float getViewerPitch();

        /**
         * Get the largest scale, up to 1, that a panel of the given size can be drawn in without
         * blocks in the world hiding any part of it. See {@link #setScale(float)}.
         *
         * @param capturedBlocks The blocks around the viewer.
         * @param width          The width of the panel, centered around the center of the panel.
         * @param height         The height of the panel, centered around the center of the panel.
         */
        float getMaxScale(CapturedBlocks capturedBlocks, float width, float height);

        /**
         * Set the scale of elements that are added from now on.
         * Elements are scaled towards the eyes of the viewer: their positions, sizes and distance
         * from the viewer are all multiplied by the scale, so they look the same to the viewer, only closer.
         * Positions and sizes that are given to the renderer are always the unscaled ones.
         */
        void setScale(float scale);

        /**
         * Set the layer of elements that are added from now on.
         * Elements of higher layers are rendered in front of elements of lower layers.
         * Elements of the same layer should not overlap.
         */
        void setLayer(int layer);

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
         * Move and resize an element over time. Elements are resized around their centers.
         *
         * @param scale    The size of the element relative to its original size.
         * @param duration The number of ticks the change takes. If 0, the element is changed immediately.
         */
        void animateElement(int elementId, float x, float y, float scale, int duration);

        /**
         * Set the color of the background of a rectangle or a tooltip.
         */
        void setColor(int elementId, int argb);

        /**
         * Set the text of a text or a tooltip.
         */
        void setText(int elementId, String text);

        /**
         * Set the item of an item.
         */
        void setItem(int elementId, ItemStack itemStack);

        /**
         * Remove elements, without unlocking the view of the viewer.
         * Elements that were already removed are ignored.
         */
        void removeElements(Collection<Integer> elementIds);

        /**
         * Remove all the elements, without unlocking the view of the viewer.
         */
        void clear();

        /**
         * Remove all the elements and give the viewer back the control over his view.
         *
         * @return Work that must be done on the main thread afterwards.
         */
        Runnable destroy();

    }

    /**
     * Inputs are reported from the threads of the network.
     */
    interface InputListener {

        void onClick(boolean rightClick);

        void onExit();

    }

}
