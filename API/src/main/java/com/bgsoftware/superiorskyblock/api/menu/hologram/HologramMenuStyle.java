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
     * Get the item that is used as the cursor of the menu, instead of {@link #getCursorText()}.
     */
    @Nullable
    ItemStack getCursorItem();

    /**
     * Get the item that is used as the cursor of the menu when it is on a button,
     * when {@link #getCursorItem()} is set.
     */
    @Nullable
    ItemStack getHoveredCursorItem();

    /**
     * Get the scale of the cursor of the menu.
     * For items, this is the size of the item.
     */
    float getCursorScale();

    /**
     * Get the scale of the cursor of the menu when it is on a button.
     */
    float getHoveredCursorScale();

    /**
     * Get whether the text of the cursor has a shadow.
     */
    boolean isCursorShadowed();

    /**
     * Get how far right the text of the cursor is drawn from the point it clicks, multiplied by its scale.
     * Used to put the tip of an arrow on that point.
     */
    float getCursorOffsetX();

    /**
     * Get how far up the text of the cursor is drawn from the point it clicks, multiplied by its scale.
     */
    float getCursorOffsetY();

    /**
     * Get how far can the cursor go past the edges of the menu, when it is limited to the menu.
     */
    float getCursorMenuMargin();

    /**
     * Get how far the cursor stays from the edges of the screen, multiplied by its scale.
     */
    float getCursorScreenMargin();

    /**
     * Get the time, in milliseconds, that must pass between two clicks.
     */
    long getClickDelay();

    /**
     * Get the speed of the cursor of the menu.
     * At a speed of 1, the cursor stays where the player looks at; higher speeds move it further
     * for the same turn of the head of the player.
     */
    float getCursorSpeed();

    /**
     * Get whether the cursor can only move over the menu.
     * Otherwise, the cursor can move over the entire screen of the player.
     */
    boolean isCursorLimitedToMenu();

    /**
     * Get the vertical field of view of players, in degrees.
     * The server cannot know the real one, so this value is used to find the edges of the screen of players.
     */
    float getScreenFov();

    /**
     * Get the ratio between the width and the height of the screen of players.
     * The server cannot know the real one, so this value is used to find the edges of the screen of players.
     */
    float getScreenAspectRatio();

    /**
     * Get whether the crosshair of the player is hidden while the menu is open.
     * This is done by putting the player in spectator mode on his side only, which also hides his hand and
     * his hotbar. Spectators do not report right clicks in the air, so only left clicks are detected.
     */
    boolean isCrosshairHidden();

    /**
     * Get how many ticks it takes for the objects of paged menus to slide when changing pages.
     * If 0, pages are changed without an animation.
     */
    int getPageAnimationDuration();

    /**
     * Get how far the objects of paged menus slide when changing pages.
     * Objects shrink while sliding, so they disappear when sliding out and appear when sliding in.
     */
    float getPageAnimationDistance();

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
     * Get the distance between the cursor and the tooltip that is shown above it.
     */
    float getTooltipOffset();

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

        Builder setCursorItem(@Nullable ItemStack cursorItem);

        Builder setHoveredCursorItem(@Nullable ItemStack hoveredCursorItem);

        Builder setCursorScale(float cursorScale);

        Builder setHoveredCursorScale(float hoveredCursorScale);

        Builder setCursorShadowed(boolean cursorShadowed);

        Builder setCursorOffsetX(float cursorOffsetX);

        Builder setCursorOffsetY(float cursorOffsetY);

        Builder setCursorMenuMargin(float cursorMenuMargin);

        Builder setCursorScreenMargin(float cursorScreenMargin);

        Builder setClickDelay(long clickDelay);

        Builder setCursorSpeed(float cursorSpeed);

        Builder setCursorLimitedToMenu(boolean cursorLimitedToMenu);

        Builder setScreenFov(float screenFov);

        Builder setScreenAspectRatio(float screenAspectRatio);

        Builder setCrosshairHidden(boolean crosshairHidden);

        Builder setPageAnimationDuration(int pageAnimationDuration);

        Builder setPageAnimationDistance(float pageAnimationDistance);

        Builder setTooltipEnabled(boolean tooltipEnabled);

        Builder setTooltipScale(float tooltipScale);

        Builder setTooltipColor(int argb);

        Builder setTooltipOffset(float tooltipOffset);

        Builder setDefaultButton(HologramButton defaultButton);

        HologramMenuStyle build();

    }

}
