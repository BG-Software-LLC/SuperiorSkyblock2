package com.bgsoftware.superiorskyblock.core.menu.hologram;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HologramButtonImpl implements HologramButton {

    public static final HologramButton DEFAULT = new Builder().build();

    @Nullable
    private final String label;
    private final float width;
    private final float height;
    private final int color;
    private final int hoveredColor;
    private final float labelScale;
    private final float itemScale;
    private final boolean labelShadowed;
    @Nullable
    private final List<String> tooltip;

    private HologramButtonImpl(Builder builder) {
        this.label = builder.label;
        this.width = builder.width;
        this.height = builder.height;
        this.color = builder.color;
        this.hoveredColor = builder.hoveredColor;
        this.labelScale = builder.labelScale;
        this.itemScale = builder.itemScale;
        this.labelShadowed = builder.labelShadowed;
        this.tooltip = builder.tooltip == null ? null : Collections.unmodifiableList(new ArrayList<>(builder.tooltip));
    }

    @Nullable
    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public float getWidth() {
        return this.width;
    }

    @Override
    public float getHeight() {
        return this.height;
    }

    @Override
    public int getColor() {
        return this.color;
    }

    @Override
    public int getHoveredColor() {
        return this.hoveredColor;
    }

    @Override
    public float getLabelScale() {
        return this.labelScale;
    }

    @Override
    public float getItemScale() {
        return this.itemScale;
    }

    @Override
    public boolean isLabelShadowed() {
        return this.labelShadowed;
    }

    @Nullable
    @Override
    public List<String> getTooltip() {
        return this.tooltip;
    }

    public static class Builder implements HologramButton.Builder {

        private String label = null;
        private float width = 2.6f;
        private float height = 0.4f;
        private int color = 0xFF555555;
        private int hoveredColor = 0xFF3B6FD9;
        private float labelScale = 1f;
        private float itemScale = 0.3f;
        private boolean labelShadowed = true;
        private List<String> tooltip = null;

        @Override
        public Builder setLabel(@Nullable String label) {
            this.label = label;
            return this;
        }

        @Override
        public Builder setWidth(float width) {
            this.width = width;
            return this;
        }

        @Override
        public Builder setHeight(float height) {
            this.height = height;
            return this;
        }

        @Override
        public Builder setColor(int argb) {
            this.color = argb;
            return this;
        }

        @Override
        public Builder setHoveredColor(int argb) {
            this.hoveredColor = argb;
            return this;
        }

        @Override
        public Builder setLabelScale(float labelScale) {
            this.labelScale = labelScale;
            return this;
        }

        @Override
        public Builder setItemScale(float itemScale) {
            this.itemScale = itemScale;
            return this;
        }

        @Override
        public Builder setLabelShadowed(boolean labelShadowed) {
            this.labelShadowed = labelShadowed;
            return this;
        }

        @Override
        public Builder setTooltip(@Nullable List<String> tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        @Override
        public HologramButton build() {
            return new HologramButtonImpl(this);
        }

    }

}
