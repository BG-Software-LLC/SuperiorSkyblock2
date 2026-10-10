package com.bgsoftware.superiorskyblock.api.menu.hologram;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import org.bukkit.inventory.ItemStack;

/**
 * Represents the appearance of a hologram menu.
 * <p>
 * All sizes are measured in blocks, and all colors are in ARGB format.
 */
public interface HologramMenuStyle {

    /**
     * Get the distance between the player and the menu.
     */
    float getDistance();

    /**
     * Get the color of the background of the menu.
     */
    int getBackgroundColor();

    /**
     * Get the item that is displayed as the background of the menu, instead of a color.
     */
    @Nullable
    ItemStack getBackgroundItem();

    /**
     * Get the minimum width of the menu.
     * The menu gets wider when its buttons do not fit in this width.
     */
    float getMinWidth();

    /**
     * Get the space between the edges of the menu and its contents.
     */
    float getPadding();

    /**
     * Get the scale of the title of the menu.
     */
    float getTitleScale();

    /**
     * Get the height that is taken by the title of the menu.
     */
    float getTitleHeight();

    /**
     * Get whether the title of the menu has a shadow.
     */
    boolean isTitleShadowed();

    /**
     * Get the scale of the body lines of the menu.
     */
    float getBodyScale();

    /**
     * Get the height that is taken by each body line of the menu.
     */
    float getBodyLineHeight();

    /**
     * Get whether the body lines of the menu have a shadow.
     */
    boolean isBodyShadowed();

    /**
     * Get the space between the title, the body and the buttons of the menu.
     */
    float getSectionsSpacing();

    /**
     * Get the space between rows of buttons.
     */
    float getRowsSpacing();

    /**
     * Get the space between buttons of the same row.
     */
    float getButtonsSpacing();

    /**
     * Get the text that is used as the cursor of the menu.
     */
    String getCursorText();

    /**
     * Get the text that is used as the cursor of the menu when it is on a button.
     */
    String getHoveredCursorText();

    /**
     * Get the scale of the cursor of the menu.
     */
    float getCursorScale();

    /**
     * Get whether tooltips are shown when the cursor is on a button.
     */
    boolean isTooltipEnabled();

    /**
     * Get the scale of the tooltips of the menu.
     */
    float getTooltipScale();

    /**
     * Get the color of the background of the tooltips of the menu.
     */
    int getTooltipColor();

    /**
     * Get the appearance of buttons that do not have one of their own.
     */
    HologramButton getDefaultButton();

    /**
     * Create a new {@link Builder} object for a new {@link HologramMenuStyle}.
     */
    static Builder newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createHologramStyleBuilder();
    }

    interface Builder {

        Builder setDistance(float distance);

        Builder setBackgroundColor(int argb);

        Builder setBackgroundItem(@Nullable ItemStack backgroundItem);

        Builder setMinWidth(float minWidth);

        Builder setPadding(float padding);

        Builder setTitleScale(float titleScale);

        Builder setTitleHeight(float titleHeight);

        Builder setTitleShadowed(boolean titleShadowed);

        Builder setBodyScale(float bodyScale);

        Builder setBodyLineHeight(float bodyLineHeight);

        Builder setBodyShadowed(boolean bodyShadowed);

        Builder setSectionsSpacing(float sectionsSpacing);

        Builder setRowsSpacing(float rowsSpacing);

        Builder setButtonsSpacing(float buttonsSpacing);

        Builder setCursorText(String cursorText);

        Builder setHoveredCursorText(String hoveredCursorText);

        Builder setCursorScale(float cursorScale);

        Builder setTooltipEnabled(boolean tooltipEnabled);

        Builder setTooltipScale(float tooltipScale);

        Builder setTooltipColor(int argb);

        Builder setDefaultButton(HologramButton defaultButton);

        HologramMenuStyle build();

    }

}
