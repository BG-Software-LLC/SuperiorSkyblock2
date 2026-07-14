package com.bgsoftware.superiorskyblock.external.inventory;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InventoryProvider_MiniMessage implements InventoryProvider {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection();

    public InventoryProvider_MiniMessage(SuperiorSkyblockPlugin plugin) {
        Log.info("Using MiniMessage as an inventory provider.");
    }

    @Override
    public void setItemMetaDisplayName(ItemMeta itemMeta, String displayName) {
        itemMeta.displayName(deserialize(displayName));
    }

    @Override
    public void setItemMetaLore(ItemMeta itemMeta, List<String> lore) {
        itemMeta.lore(new SequentialListBuilder<Component>().build(lore, InventoryProvider_MiniMessage::deserialize));
    }

    @Override
    public Inventory createInventory(InventoryHolder inventoryHolder, InventoryType inventoryType, String title) {
        return Bukkit.createInventory(inventoryHolder, inventoryType, deserialize(title));
    }

    @Override
    public Inventory createInventory(InventoryHolder inventoryHolder, int size, String title) {
        return Bukkit.createInventory(inventoryHolder, size, deserialize(title));
    }

    private static Component deserialize(String message) {
        if (message.indexOf(ChatColor.COLOR_CHAR) >= 0) {
            return removeItalic(LEGACY_COMPONENT_SERIALIZER.deserialize(message));
        }

        try {
            return removeItalic(MINI_MESSAGE.deserialize(message));
        } catch (ParsingException exception) {
            return removeItalic(LEGACY_COMPONENT_SERIALIZER.deserialize(message));
        }
    }

    // 'NOT_SET' for Italic is interpreted by Minecraft as 'TRUE', so we need to set it to 'FALSE'.
    private static Component removeItalic(Component component) {
        if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
            component = component.decoration(TextDecoration.ITALIC, false);
        }

        return component;
    }

}
