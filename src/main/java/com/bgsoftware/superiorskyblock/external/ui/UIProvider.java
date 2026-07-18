package com.bgsoftware.superiorskyblock.external.ui;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public interface UIProvider {

    /**
     * Create an action bar message component.
     *
     * @param message The action bar text.
     * @return The created message component.
     */
    IMessageComponent createActionBarComponent(@Nullable String message);

    /**
     * Create a raw message component.
     *
     * @param message The raw text.
     * @return The created message component.
     */
    IMessageComponent createRawMessageComponent(@Nullable String message);

    /**
     * Create a complex message component.
     *
     * @param message The content of the complex message.
     * @param command The command to be executed when clicked on the message.
     * @param suggest The command to suggest when clicked on the message.
     * @param tooltip The text to show when hovering over the message.
     * @return The created message component.
     */
    IMessageComponent createComplexMessageComponent(@Nullable String message, @Nullable String command,
                                                    @Nullable String suggest, @Nullable String tooltip);

    /**
     * Create a complex message component.
     *
     * @param components The components of the complex message.
     * @return The created message component.
     */
    IMessageComponent createComplexMessageComponent(@Nullable BaseComponent[] components);

    /**
     * Create a title message component.
     *
     * @param titleMessage    The title text.
     * @param subtitleMessage The subtitle text.
     * @param fadeIn          The duration of the fade-in phase, in ticks.
     * @param stay            The duration of the stay phase, in ticks.
     * @param fadeOut         The duration of the fade-out phase, in ticks.
     * @return The created message component.
     */
    IMessageComponent createTitleComponent(@Nullable String titleMessage, @Nullable String subtitleMessage,
                                           int fadeIn, int stay, int fadeOut);

    /**
     * Set the display name of the given item meta.
     *
     * @param itemMeta    The item meta to modify.
     * @param displayName The display name to set.
     */
    void setItemMetaDisplayName(ItemMeta itemMeta, String displayName);

    /**
     * Set the lore of the given item meta.
     *
     * @param itemMeta The item meta to modify.
     * @param lore     The lore to set.
     */
    void setItemMetaLore(ItemMeta itemMeta, List<String> lore);

    /**
     * Create a chest inventory with the specified size.
     *
     * @param inventoryHolder The inventory holder.
     * @param size            The inventory size.
     * @param title           The inventory title.
     * @return The created inventory.
     */
    Inventory createInventory(InventoryHolder inventoryHolder, int size, String title);

    /**
     * Create an inventory with the specified type.
     *
     * @param inventoryHolder The inventory holder.
     * @param inventoryType   The inventory type.
     * @param title           The inventory title.
     * @return The created inventory.
     */
    Inventory createInventory(InventoryHolder inventoryHolder, InventoryType inventoryType, String title);

}
