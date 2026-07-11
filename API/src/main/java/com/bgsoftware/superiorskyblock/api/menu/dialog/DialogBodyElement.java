package com.bgsoftware.superiorskyblock.api.menu.dialog;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.annotations.Size;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a body element in a dialog.
 */
public interface DialogBodyElement {

    /**
     * Creates a new text-based body element for dialog UI.
     *
     * @param text       The text to show in the dialog.
     * @param textConfig Configuration for the body element.
     */
    static DialogBodyElement fromText(String text, @Nullable TextConfig textConfig) {
        return SuperiorSkyblockAPI.getMenus().createDialogBodyTextElement(text, textConfig);
    }

    /**
     * Creates a new item-based body element for dialog UI.
     *
     * @param itemStack  The item to show in the dialog.
     * @param itemConfig Configuration for the body element.
     */
    static DialogBodyElement fromItem(ItemStack itemStack, @Nullable ItemConfig itemConfig) {
        return SuperiorSkyblockAPI.getMenus().createDialogBodyItemElement(itemStack, itemConfig);
    }

    /**
     * Configuration for text-based body elements.
     */
    interface TextConfig {

        int DEFAULT_WIDTH = 200;

        static TextConfig create() {
            return SuperiorSkyblockAPI.getMenus().createTextConfig();
        }

        /**
         * Set the width of the text element.
         *
         * @param width The width of the element.
         */
        TextConfig setWidth(@Size int width);

    }

    /**
     * Configuration for item-based body elements.
     */
    interface ItemConfig {

        int DEFAULT_WIDTH = 16;
        int DEFAULT_HEIGHT = 16;

        static ItemConfig create() {
            return SuperiorSkyblockAPI.getMenus().createItemConfig();
        }

        /**
         * Set the description of the item element.
         *
         * @param description The description to set.
         */
        ItemConfig setDescription(@Nullable DialogBodyElement description);

        /**
         * Set whether the item should have its decorations shown
         *
         * @param showDecorations Whether to show the item decorations
         */
        ItemConfig setShowDecorations(boolean showDecorations);

        /**
         * Set whether the item should have its tooltip shown
         *
         * @param showTooltip Whether to show the item tooltip
         */
        ItemConfig setShowTooltip(boolean showTooltip);

        /**
         * Set the width of the item element.
         *
         * @param width The width of the element.
         */
        ItemConfig setWidth(@Size int width);

        /**
         * Set the height of the item element.
         *
         * @param height The height of the element.
         */
        ItemConfig setHeight(@Size int height);
    }

}
