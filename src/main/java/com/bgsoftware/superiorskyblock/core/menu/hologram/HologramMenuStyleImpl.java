package com.bgsoftware.superiorskyblock.core.menu.hologram;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramButton;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramMenuStyle;
import com.google.common.base.Preconditions;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class HologramMenuStyleImpl implements HologramMenuStyle {

    public static final HologramMenuStyle DEFAULT = new Builder().build();

    private final float distance;
    private final int backgroundColor;
    @Nullable
    private final ItemStack backgroundItem;
    private final float minWidth;
    private final float padding;
    private final float titleScale;
    private final float titleHeight;
    private final boolean titleShadowed;
    private final float bodyScale;
    private final float bodyLineHeight;
    private final boolean bodyShadowed;
    private final float sectionsSpacing;
    private final float rowsSpacing;
    private final float buttonsSpacing;
    private final String cursorText;
    private final String hoveredCursorText;
    private final float cursorScale;
    private final boolean tooltipEnabled;
    private final float tooltipScale;
    private final int tooltipColor;
    private final HologramButton defaultButton;

    private HologramMenuStyleImpl(Builder builder) {
        this.distance = builder.distance;
        this.backgroundColor = builder.backgroundColor;
        this.backgroundItem = builder.backgroundItem;
        this.minWidth = builder.minWidth;
        this.padding = builder.padding;
        this.titleScale = builder.titleScale;
        this.titleHeight = builder.titleHeight;
        this.titleShadowed = builder.titleShadowed;
        this.bodyScale = builder.bodyScale;
        this.bodyLineHeight = builder.bodyLineHeight;
        this.bodyShadowed = builder.bodyShadowed;
        this.sectionsSpacing = builder.sectionsSpacing;
        this.rowsSpacing = builder.rowsSpacing;
        this.buttonsSpacing = builder.buttonsSpacing;
        this.cursorText = builder.cursorText;
        this.hoveredCursorText = builder.hoveredCursorText;
        this.cursorScale = builder.cursorScale;
        this.tooltipEnabled = builder.tooltipEnabled;
        this.tooltipScale = builder.tooltipScale;
        this.tooltipColor = builder.tooltipColor;
        this.defaultButton = builder.defaultButton;
    }

    @Override
    public float getDistance() {
        return this.distance;
    }

    @Override
    public int getBackgroundColor() {
        return this.backgroundColor;
    }

    @Nullable
    @Override
    public ItemStack getBackgroundItem() {
        return this.backgroundItem == null ? null : this.backgroundItem.clone();
    }

    @Override
    public float getMinWidth() {
        return this.minWidth;
    }

    @Override
    public float getPadding() {
        return this.padding;
    }

    @Override
    public float getTitleScale() {
        return this.titleScale;
    }

    @Override
    public float getTitleHeight() {
        return this.titleHeight;
    }

    @Override
    public boolean isTitleShadowed() {
        return this.titleShadowed;
    }

    @Override
    public float getBodyScale() {
        return this.bodyScale;
    }

    @Override
    public float getBodyLineHeight() {
        return this.bodyLineHeight;
    }

    @Override
    public boolean isBodyShadowed() {
        return this.bodyShadowed;
    }

    @Override
    public float getSectionsSpacing() {
        return this.sectionsSpacing;
    }

    @Override
    public float getRowsSpacing() {
        return this.rowsSpacing;
    }

    @Override
    public float getButtonsSpacing() {
        return this.buttonsSpacing;
    }

    @Override
    public String getCursorText() {
        return this.cursorText;
    }

    @Override
    public String getHoveredCursorText() {
        return this.hoveredCursorText;
    }

    @Override
    public float getCursorScale() {
        return this.cursorScale;
    }

    @Override
    public boolean isTooltipEnabled() {
        return this.tooltipEnabled;
    }

    @Override
    public float getTooltipScale() {
        return this.tooltipScale;
    }

    @Override
    public int getTooltipColor() {
        return this.tooltipColor;
    }

    @Override
    public HologramButton getDefaultButton() {
        return this.defaultButton;
    }

    public static class Builder implements HologramMenuStyle.Builder {

        private float distance = 3f;
        private int backgroundColor = 0xC0101010;
        private ItemStack backgroundItem = null;
        private float minWidth = 3.2f;
        private float padding = 0.25f;
        private float titleScale = 1.2f;
        private float titleHeight = 0.45f;
        private boolean titleShadowed = true;
        private float bodyScale = 0.8f;
        private float bodyLineHeight = 0.28f;
        private boolean bodyShadowed = true;
        private float sectionsSpacing = 0.15f;
        private float rowsSpacing = 0.15f;
        private float buttonsSpacing = 0.15f;
        private String cursorText = ChatColor.WHITE + "+";
        private String hoveredCursorText = ChatColor.AQUA + "+";
        private float cursorScale = 1f;
        private boolean tooltipEnabled = true;
        private float tooltipScale = 0.6f;
        private int tooltipColor = 0xE0100010;
        private HologramButton defaultButton = HologramButtonImpl.DEFAULT;

        @Override
        public Builder setDistance(float distance) {
            Preconditions.checkArgument(distance > 0f, "distance must be positive");
            this.distance = distance;
            return this;
        }

        @Override
        public Builder setBackgroundColor(int argb) {
            this.backgroundColor = argb;
            return this;
        }

        @Override
        public Builder setBackgroundItem(@Nullable ItemStack backgroundItem) {
            this.backgroundItem = backgroundItem == null ? null : backgroundItem.clone();
            return this;
        }

        @Override
        public Builder setMinWidth(float minWidth) {
            this.minWidth = minWidth;
            return this;
        }

        @Override
        public Builder setPadding(float padding) {
            this.padding = padding;
            return this;
        }

        @Override
        public Builder setTitleScale(float titleScale) {
            this.titleScale = titleScale;
            return this;
        }

        @Override
        public Builder setTitleHeight(float titleHeight) {
            this.titleHeight = titleHeight;
            return this;
        }

        @Override
        public Builder setTitleShadowed(boolean titleShadowed) {
            this.titleShadowed = titleShadowed;
            return this;
        }

        @Override
        public Builder setBodyScale(float bodyScale) {
            this.bodyScale = bodyScale;
            return this;
        }

        @Override
        public Builder setBodyLineHeight(float bodyLineHeight) {
            this.bodyLineHeight = bodyLineHeight;
            return this;
        }

        @Override
        public Builder setBodyShadowed(boolean bodyShadowed) {
            this.bodyShadowed = bodyShadowed;
            return this;
        }

        @Override
        public Builder setSectionsSpacing(float sectionsSpacing) {
            this.sectionsSpacing = sectionsSpacing;
            return this;
        }

        @Override
        public Builder setRowsSpacing(float rowsSpacing) {
            this.rowsSpacing = rowsSpacing;
            return this;
        }

        @Override
        public Builder setButtonsSpacing(float buttonsSpacing) {
            this.buttonsSpacing = buttonsSpacing;
            return this;
        }

        @Override
        public Builder setCursorText(String cursorText) {
            Preconditions.checkNotNull(cursorText, "cursorText parameter cannot be null");
            this.cursorText = cursorText;
            return this;
        }

        @Override
        public Builder setHoveredCursorText(String hoveredCursorText) {
            Preconditions.checkNotNull(hoveredCursorText, "hoveredCursorText parameter cannot be null");
            this.hoveredCursorText = hoveredCursorText;
            return this;
        }

        @Override
        public Builder setCursorScale(float cursorScale) {
            this.cursorScale = cursorScale;
            return this;
        }

        @Override
        public Builder setTooltipEnabled(boolean tooltipEnabled) {
            this.tooltipEnabled = tooltipEnabled;
            return this;
        }

        @Override
        public Builder setTooltipScale(float tooltipScale) {
            this.tooltipScale = tooltipScale;
            return this;
        }

        @Override
        public Builder setTooltipColor(int argb) {
            this.tooltipColor = argb;
            return this;
        }

        @Override
        public Builder setDefaultButton(HologramButton defaultButton) {
            Preconditions.checkNotNull(defaultButton, "defaultButton parameter cannot be null");
            this.defaultButton = defaultButton;
            return this;
        }

        @Override
        public HologramMenuStyle build() {
            return new HologramMenuStyleImpl(this);
        }

    }

}
