package com.bgsoftware.superiorskyblock.api.menu.dialog;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.google.common.base.Preconditions;
import org.bukkit.inventory.ItemStack;

public interface DialogBodyElement {

    static DialogBodyElement fromText(String text, @Nullable TextConfig textConfig) {
        return SuperiorSkyblockAPI.getMenus().createDialogBodyTextElement(text, textConfig);
    }

    static DialogBodyElement fromItem(ItemStack itemStack) {
        return fromItem(itemStack, null);
    }

    static DialogBodyElement fromItem(ItemStack itemStack, @Nullable ItemConfig itemConfig) {
        return SuperiorSkyblockAPI.getMenus().createDialogBodyItemElement(itemStack, itemConfig);
    }

    class TextConfig {

        private int width = 200;

        public TextConfig() {

        }

        public TextConfig setWidth(int width) {
            Preconditions.checkArgument(width > 0, "width must be a positive number");
            this.width = width;
            return this;
        }

        public int getWidth() {
            return width;
        }

    }

    class ItemConfig {

        @Nullable
        private DialogBodyElement description = null;
        private boolean showDecorations = true;
        private boolean showTooltip = true;
        private int width = 16;
        private int height = 16;

        public ItemConfig() {

        }

        public ItemConfig setDescription(@Nullable DialogBodyElement description) {
            this.description = description;
            return this;
        }

        public DialogBodyElement getDescription() {
            return this.description;
        }

        public ItemConfig setShowDecorations(boolean showDecorations) {
            this.showDecorations = showDecorations;
            return this;
        }

        public boolean isShowDecorations() {
            return this.showDecorations;
        }

        public ItemConfig setShowTooltip(boolean showTooltip) {
            this.showTooltip = showTooltip;
            return this;
        }

        public boolean isShowTooltip() {
            return this.showTooltip;
        }

        public ItemConfig setWidth(int width) {
            Preconditions.checkArgument(width >= 1 && width <= 256, "width must be between 1 and 256");
            this.width = width;
            return this;
        }

        public int getWidth() {
            return width;
        }

        public ItemConfig setHeight(int height) {
            Preconditions.checkArgument(height >= 1 && height <= 256, "height must be between 1 and 256");
            this.height = height;
            return this;
        }

        public int getHeight() {
            return height;
        }
    }

}
