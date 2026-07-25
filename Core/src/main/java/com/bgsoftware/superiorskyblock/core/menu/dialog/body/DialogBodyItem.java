package com.bgsoftware.superiorskyblock.core.menu.dialog.body;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogBodyElement;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.google.common.base.Preconditions;

import java.util.Objects;

public class DialogBodyItem implements DialogBodyElement {

    private static final Config DEFAULT_CONFIG = new Config();

    private final TemplateItem item;
    private final Config config;

    public DialogBodyItem(TemplateItem item, @Nullable ItemConfig itemConfig) {
        this.item = item;
        this.config = itemConfig == null || DEFAULT_CONFIG.equals(itemConfig) ? DEFAULT_CONFIG : (Config) itemConfig;
    }

    public TemplateItem getItem() {
        return item;
    }

    @Nullable
    public DialogBodyElement getDescription() {
        return this.config.description;
    }

    public boolean isShowDecorations() {
        return this.config.showDecorations;
    }

    public boolean isShowTooltip() {
        return this.config.showTooltip;
    }

    public int getWidth() {
        return this.config.width;
    }

    public int getHeight() {
        return this.config.height;
    }

    public static class Config implements ItemConfig {

        @Nullable
        private DialogBodyElement description = null;
        private boolean showDecorations = true;
        private boolean showTooltip = true;
        private int width = ItemConfig.DEFAULT_WIDTH;
        private int height = ItemConfig.DEFAULT_HEIGHT;

        @Override
        public Config setDescription(@Nullable DialogBodyElement description) {
            this.description = description;
            return this;
        }

        @Override
        public Config setShowDecorations(boolean showDecorations) {
            this.showDecorations = showDecorations;
            return this;
        }

        @Override
        public Config setShowTooltip(boolean showTooltip) {
            this.showTooltip = showTooltip;
            return this;
        }

        @Override
        public Config setWidth(int width) {
            Preconditions.checkArgument(width > 0 && width <= 256, "width must be between 1 and 256");
            this.width = width;
            return this;
        }

        @Override
        public Config setHeight(int height) {
            Preconditions.checkArgument(height > 0 && height <= 256, "height must be between 1 and 256");
            this.height = height;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Config config = (Config) o;
            return showDecorations == config.showDecorations && showTooltip == config.showTooltip &&
                    width == config.width && height == config.height && Objects.equals(description, config.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(description, showDecorations, showTooltip, width, height);
        }

    }

}
