package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandGenerateBlockEvent;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Optional;

public class MMOItemsHook {

    private static final ReflectField<MMOItem> CUSTOM_BLOCK_MMOITEM = new ReflectField<>(
            CustomBlock.class, MMOItem.class, Modifier.PRIVATE | Modifier.FINAL, 1);

    private static final String MMO_ITEMS_PREFIX = "MMO_ITEMS";
    private static final Key[] CUSTOM_KEYS = collectCustomKeys();

    private static boolean registered = false;

    private static SuperiorSkyblockPlugin plugin;

    public static void register(SuperiorSkyblockPlugin plugin) {
        MMOItemsHook.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new ListenerImpl(), plugin);
        if (!registered) {
            registered = true;
            plugin.getBlockValues().registerKeyParser(new MMOItemsKeyParser(), CUSTOM_KEYS);
        }
    }

    private static Key[] collectCustomKeys() {
        KeySet customKeys = KeySets.createHashSet(KeyIndicator.MATERIAL);
        for (Type type : MMOItems.plugin.getTypes().getAll()) {
            customKeys.add(Keys.of(type.getItem()));
        }
        // Custom blocks
        customKeys.add(Keys.of(Material.valueOf("BROWN_MUSHROOM_BLOCK")));
        customKeys.add(Keys.of(Material.valueOf("MUSHROOM_STEM")));
        customKeys.add(Keys.of(Material.valueOf("RED_MUSHROOM_BLOCK")));
        return customKeys.toArray(new Key[0]);
    }

    private static class ListenerImpl implements Listener {

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onIslandGenerateBlock(IslandGenerateBlockEvent event) {
            if (!event.getBlock().getGlobalKey().equals(MMO_ITEMS_PREFIX))
                return;

            MMOItem mmoItem = MMOItems.plugin.getMMOItem(Type.BLOCK, event.getBlock().getSubKey());
            if (mmoItem != null) {
                DoubleData doubleData = (DoubleData) mmoItem.getData(ItemStats.BLOCK_ID);
                if (doubleData != null) {
                    CustomBlock customBlock = MMOItems.plugin.getCustomBlocks().getBlock((int) doubleData.getValue());
                    if (customBlock != null) {
                        event.setPlaceBlock(false);
                        event.getLocation().getBlock().setBlockData(customBlock.getState().getBlockData());
                        return;
                    }
                }
            }

            event.setCancelled(true);
        }

    }

    private static class MMOItemsKeyParser implements CustomKeyParser {

        @Override
        public Key getCustomKey(Location location) {
            Optional<CustomBlock> customBlock = MMOItems.plugin.getCustomBlocks().getFromBlock(location.getBlock().getBlockData());
            if (customBlock.isEmpty())
                return null;
            MMOItem mmoItem = CUSTOM_BLOCK_MMOITEM.get(customBlock.get());
            return Keys.of(MMO_ITEMS_PREFIX, mmoItem.getId().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public Key getCustomKey(ItemStack itemStack, Key def) {
            NBTItem nbtItem = NBTItem.get(itemStack);
            String id = MMOItems.getID(nbtItem);
            if (id == null)
                return def;
            Type type = MMOItems.getType(nbtItem);
            if (type == null)
                return def;
            return Keys.of(MMO_ITEMS_PREFIX, (id + "_" + type.getId()).toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public boolean isCustomKey(Key key) {
            return key.getGlobalKey().equals(MMO_ITEMS_PREFIX);
        }

        @Override
        @Nullable
        public ItemStack getCustomKeyItem(Key key) {
            MMOItem mmoItem = MMOItems.plugin.getMMOItem(Type.BLOCK, key.getSubKey());
            if (mmoItem == null)
                return null;
            return mmoItem.newBuilder().build();
        }

    }

}
