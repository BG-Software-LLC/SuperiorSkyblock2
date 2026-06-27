package com.bgsoftware.superiorskyblock.api.menu.dialog;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import org.bukkit.inventory.ItemStack;

public interface DialogBodyElement {

    static DialogBodyElement fromText(String text) {
        return SuperiorSkyblockAPI.getMenus().createDialogBodyTextElement(text);
    }

    static DialogBodyElement fromItem(ItemStack itemStack) {
        return fromItem(itemStack, null);
    }

    static DialogBodyElement fromItem(ItemStack itemStack, @Nullable ItemConfig itemConfig) {
        return SuperiorSkyblockAPI.getMenus().createDialogBodyItemElement(itemStack, itemConfig);
    }

    class ItemConfig {

        @Nullable
        private String description = null;
        private boolean showDecorations = true;
        private boolean showTooltip = true;

        public ItemConfig() {

        }

        public ItemConfig setDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        public String getDescription() {
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
    }

}
