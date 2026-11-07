package com.bgsoftware.superiorskyblock.core.menu.hologram;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramButton;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramMenuStyle;
import com.google.common.base.Preconditions;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class HologramMenuStyleImpl implements HologramMenuStyle {

    /* The arrowhead-shaped pointer of unicode (U+1FBB0), which looks like the cursor of a mouse. */
    public static final String POINTER = "\uD83E\uDFB0";
    /* The tip of the pointer is at the top left of its text, 1 pixel to the left of its center and 4 pixels above it. */
    public static final float ARROW_OFFSET_X = 0.025f;
    public static final float ARROW_OFFSET_Y = -0.1f;

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
    @Nullable
    private final ItemStack cursorItem;
    @Nullable
    private final ItemStack hoveredCursorItem;
    private final float cursorScale;
    private final float hoveredCursorScale;
    private final boolean cursorShadowed;
    private final float cursorOffsetX;
    private final float cursorOffsetY;
    private final float cursorMenuMargin;
    private final float cursorScreenMargin;
    private final long clickDelay;
    private final float cursorSpeed;
    private final boolean cursorLimitedToMenu;
    private final float screenFov;
    private final float screenAspectRatio;
    private final boolean crosshairHidden;
    private final int pageAnimationDuration;
    private final float pageAnimationDistance;
    private final boolean tooltipEnabled;
    private final float tooltipScale;
    private final int tooltipColor;
    private final float tooltipOffset;
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
        this.cursorItem = builder.cursorItem;
        this.hoveredCursorItem = builder.hoveredCursorItem;
        this.cursorScale = builder.cursorScale;
        this.hoveredCursorScale = builder.hoveredCursorScale;
        this.cursorShadowed = builder.cursorShadowed;
        this.cursorOffsetX = builder.cursorOffsetX;
        this.cursorOffsetY = builder.cursorOffsetY;
        this.cursorMenuMargin = builder.cursorMenuMargin;
        this.cursorScreenMargin = builder.cursorScreenMargin;
        this.clickDelay = builder.clickDelay;
        this.cursorSpeed = builder.cursorSpeed;
        this.cursorLimitedToMenu = builder.cursorLimitedToMenu;
        this.screenFov = builder.screenFov;
        this.screenAspectRatio = builder.screenAspectRatio;
        this.crosshairHidden = builder.crosshairHidden;
        this.pageAnimationDuration = builder.pageAnimationDuration;
        this.pageAnimationDistance = builder.pageAnimationDistance;
        this.tooltipEnabled = builder.tooltipEnabled;
        this.tooltipScale = builder.tooltipScale;
        this.tooltipColor = builder.tooltipColor;
        this.tooltipOffset = builder.tooltipOffset;
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

    @Nullable
    @Override
    public ItemStack getCursorItem() {
        return this.cursorItem == null ? null : this.cursorItem.clone();
    }

    @Nullable
    @Override
    public ItemStack getHoveredCursorItem() {
        return this.hoveredCursorItem == null ? null : this.hoveredCursorItem.clone();
    }

    @Override
    public float getCursorScale() {
        return this.cursorScale;
    }

    @Override
    public float getHoveredCursorScale() {
        return this.hoveredCursorScale;
    }

    @Override
    public boolean isCursorShadowed() {
        return this.cursorShadowed;
    }

    @Override
    public float getCursorOffsetX() {
        return this.cursorOffsetX;
    }

    @Override
    public float getCursorOffsetY() {
        return this.cursorOffsetY;
    }

    @Override
    public float getCursorMenuMargin() {
        return this.cursorMenuMargin;
    }

    @Override
    public float getCursorScreenMargin() {
        return this.cursorScreenMargin;
    }

    @Override
    public long getClickDelay() {
        return this.clickDelay;
    }

    @Override
    public float getCursorSpeed() {
        return this.cursorSpeed;
    }

    @Override
    public boolean isCursorLimitedToMenu() {
        return this.cursorLimitedToMenu;
    }

    @Override
    public float getScreenFov() {
        return this.screenFov;
    }

    @Override
    public float getScreenAspectRatio() {
        return this.screenAspectRatio;
    }

    @Override
    public boolean isCrosshairHidden() {
        return this.crosshairHidden;
    }

    @Override
    public int getPageAnimationDuration() {
        return this.pageAnimationDuration;
    }

    @Override
    public float getPageAnimationDistance() {
        return this.pageAnimationDistance;
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
    public float getTooltipOffset() {
        return this.tooltipOffset;
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
        private String cursorText = ChatColor.GRAY + POINTER;
        private String hoveredCursorText = ChatColor.WHITE + POINTER;
        private ItemStack cursorItem = null;
        private ItemStack hoveredCursorItem = null;
        private float cursorScale = 0.75f;
        private float hoveredCursorScale = 0.75f;
        private boolean cursorShadowed = true;
        private float cursorOffsetX = ARROW_OFFSET_X;
        private float cursorOffsetY = ARROW_OFFSET_Y;
        private float cursorMenuMargin = 0.2f;
        private float cursorScreenMargin = 0.1f;
        private long clickDelay = 250L;
        private float cursorSpeed = 1.5f;
        private boolean cursorLimitedToMenu = false;
        // The defaults of Minecraft: a field of view of 70 degrees, on a 16:9 screen.
        private float screenFov = 70f;
        private float screenAspectRatio = 16f / 9f;
        private boolean crosshairHidden = true;
        private int pageAnimationDuration = 6;
        private float pageAnimationDistance = 0.6f;
        private boolean tooltipEnabled = true;
        private float tooltipScale = 0.6f;
        private int tooltipColor = 0xE0100010;
        private float tooltipOffset = 0.15f;
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
        public Builder setCursorItem(@Nullable ItemStack cursorItem) {
            this.cursorItem = cursorItem == null ? null : cursorItem.clone();
            return this;
        }

        @Override
        public Builder setHoveredCursorItem(@Nullable ItemStack hoveredCursorItem) {
            this.hoveredCursorItem = hoveredCursorItem == null ? null : hoveredCursorItem.clone();
            return this;
        }

        @Override
        public Builder setCursorScale(float cursorScale) {
            this.cursorScale = cursorScale;
            return this;
        }

        @Override
        public Builder setHoveredCursorScale(float hoveredCursorScale) {
            this.hoveredCursorScale = hoveredCursorScale;
            return this;
        }

        @Override
        public Builder setCursorShadowed(boolean cursorShadowed) {
            this.cursorShadowed = cursorShadowed;
            return this;
        }

        @Override
        public Builder setCursorOffsetX(float cursorOffsetX) {
            this.cursorOffsetX = cursorOffsetX;
            return this;
        }

        @Override
        public Builder setCursorOffsetY(float cursorOffsetY) {
            this.cursorOffsetY = cursorOffsetY;
            return this;
        }

        @Override
        public Builder setCursorMenuMargin(float cursorMenuMargin) {
            this.cursorMenuMargin = cursorMenuMargin;
            return this;
        }

        @Override
        public Builder setCursorScreenMargin(float cursorScreenMargin) {
            this.cursorScreenMargin = cursorScreenMargin;
            return this;
        }

        @Override
        public Builder setClickDelay(long clickDelay) {
            Preconditions.checkArgument(clickDelay >= 0, "clickDelay cannot be negative");
            this.clickDelay = clickDelay;
            return this;
        }

        @Override
        public Builder setCursorSpeed(float cursorSpeed) {
            Preconditions.checkArgument(cursorSpeed > 0f, "cursorSpeed must be positive");
            this.cursorSpeed = cursorSpeed;
            return this;
        }

        @Override
        public Builder setCursorLimitedToMenu(boolean cursorLimitedToMenu) {
            this.cursorLimitedToMenu = cursorLimitedToMenu;
            return this;
        }

        @Override
        public Builder setScreenFov(float screenFov) {
            Preconditions.checkArgument(screenFov > 0f && screenFov < 180f, "screenFov must be between 0 and 180");
            this.screenFov = screenFov;
            return this;
        }

        @Override
        public Builder setScreenAspectRatio(float screenAspectRatio) {
            Preconditions.checkArgument(screenAspectRatio > 0f, "screenAspectRatio must be positive");
            this.screenAspectRatio = screenAspectRatio;
            return this;
        }

        @Override
        public Builder setCrosshairHidden(boolean crosshairHidden) {
            this.crosshairHidden = crosshairHidden;
            return this;
        }

        @Override
        public Builder setPageAnimationDuration(int pageAnimationDuration) {
            Preconditions.checkArgument(pageAnimationDuration >= 0, "pageAnimationDuration cannot be negative");
            this.pageAnimationDuration = pageAnimationDuration;
            return this;
        }

        @Override
        public Builder setPageAnimationDistance(float pageAnimationDistance) {
            this.pageAnimationDistance = pageAnimationDistance;
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
        public Builder setTooltipOffset(float tooltipOffset) {
            this.tooltipOffset = tooltipOffset;
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
