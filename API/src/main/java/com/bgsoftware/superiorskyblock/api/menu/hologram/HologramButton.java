package com.bgsoftware.superiorskyblock.api.menu.hologram;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

import java.util.List;

/**
 * Represents the appearance of a button in a hologram menu.
 * The item of the button, if there is one, is the item of the
 * {@link com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton} this object is attached to.
 * <p>
 * All sizes are measured in blocks, and all colors are in ARGB format.
 */
public interface HologramButton {

    /**
     * Get the label of the button.
     */
    @Nullable
    String getLabel();

    /**
     * Get the width of the button.
     */
    float getWidth();

    /**
     * Get the height of the button.
     */
    float getHeight();

    /**
     * Get the color of the background of the button.
     */
    int getColor();

    /**
     * Get the color of the background of the button when the cursor is on it.
     */
    int getHoveredColor();

    /**
     * Get the scale of the label of the button.
     */
    float getLabelScale();

    /**
     * Get the size of the item that is displayed in the button.
     */
    float getItemScale();

    /**
     * Get whether the label of the button has a shadow.
     */
    boolean isLabelShadowed();

    /**
     * Get the lines of the tooltip that is shown when the cursor is on the button.
     * If null, the name and the lore of the item of the button are used as the tooltip.
     */
    @Nullable
    List<String> getTooltip();

    /**
     * Create a new {@link Builder} object for a new {@link HologramButton}.
     */
    static Builder newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createHologramButtonBuilder();
    }

    /**
     * Create a new {@link Builder} object that has the same values as another {@link HologramButton}.
     *
     * @param other The button to copy the values from.
     */
    static Builder newBuilder(HologramButton other) {
        return newBuilder()
                .setLabel(other.getLabel())
                .setWidth(other.getWidth())
                .setHeight(other.getHeight())
                .setColor(other.getColor())
                .setHoveredColor(other.getHoveredColor())
                .setLabelScale(other.getLabelScale())
                .setItemScale(other.getItemScale())
                .setLabelShadowed(other.isLabelShadowed())
                .setTooltip(other.getTooltip());
    }

    interface Builder {

        Builder setLabel(@Nullable String label);

        Builder setWidth(float width);

        Builder setHeight(float height);

        Builder setColor(int argb);

        Builder setHoveredColor(int argb);

        Builder setLabelScale(float labelScale);

        Builder setItemScale(float itemScale);

        Builder setLabelShadowed(boolean labelShadowed);

        Builder setTooltip(@Nullable List<String> tooltip);

        HologramButton build();

    }

}
