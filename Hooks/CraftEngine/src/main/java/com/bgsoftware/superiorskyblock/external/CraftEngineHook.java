package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandGenerateBlockEvent;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class CraftEngineHook {

    private static final String CRAFTENGINE_PREFIX = "CRAFTENGINE";
    private static final KeySet CUSTOM_ITEM_KEYS = collectCustomItemKeys();
    private static final KeySet CUSTOM_BLOCK_KEYS = collectCustomBlockKeys();
    private static final KeyMap<ItemStack> CUSTOM_ITEM_CACHE = collectCustomItemsCache();
    private static final KeyMap<CustomBlock> CUSTOM_ITEM_TO_BLOCK_CACHE = collectCustomItemToBlocksCache();

    public static void register(SuperiorSkyblockPlugin plugin) {
        plugin.getBlockValues().registerKeyParser(new CraftEngineKeyParser(), collectCustomKeys());
        plugin.getServer().getPluginManager().registerEvents(new ListenerImpl(), plugin);
    }

    private static KeySet collectCustomItemKeys() {
        KeySet customItemKeys = KeySets.createHashSet(KeyIndicator.MATERIAL);
        for (net.momirealms.craftengine.core.util.Key itemKey : CraftEngine.instance().itemManager().items()) {
            CustomItem<ItemStack> customItem = CraftEngineItems.byId(itemKey);
            if (customItem != null) {
                ItemStack itemStack = customItem.buildItemStack();
                customItemKeys.add(Keys.of(itemStack.getType()));
            }
        }
        return customItemKeys;
    }

    private static KeySet collectCustomBlockKeys() {
        KeySet customBlockKeys = KeySets.createHashSet(KeyIndicator.MATERIAL);
        for (CustomBlock customBlock : CraftEngine.instance().blockManager().blocks().values()) {
            BlockData blockData = CraftEngineBlocks.getBukkitBlockData(customBlock.defaultState());
            customBlockKeys.add(Keys.of(blockData.getMaterial()));
        }
        return customBlockKeys;
    }

    private static KeyMap<ItemStack> collectCustomItemsCache() {
        KeyMap<ItemStack> customItemsCache = KeyMaps.createHashMap(KeyIndicator.CUSTOM);

        for (net.momirealms.craftengine.core.util.Key itemKey : CraftEngine.instance().itemManager().items()) {
            CustomItem<ItemStack> customItem = CraftEngineItems.byId(itemKey);
            if (customItem != null) {
                ItemStack itemStack = customItem.buildItemStack();
                Key ssbItemKey = Keys.of(CRAFTENGINE_PREFIX, itemKey.value().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
                customItemsCache.put(ssbItemKey, itemStack);
            }
        }

        return customItemsCache;
    }

    private static KeyMap<CustomBlock> collectCustomItemToBlocksCache() {
        KeyMap<CustomBlock> customItemToBlocksCache = KeyMaps.createHashMap(KeyIndicator.CUSTOM);

        CraftEngine.instance().blockManager().blocks().forEach((blockKey, customBlock) -> {
            Key ssbBlockKey = Keys.of(CRAFTENGINE_PREFIX, blockKey.value().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
            customItemToBlocksCache.put(ssbBlockKey, customBlock);
        });

        return customItemToBlocksCache;
    }

    private static Key[] collectCustomKeys() {
        KeySet customKeys = KeySets.createHashSet(KeyIndicator.MATERIAL);
        customKeys.addAll(CUSTOM_ITEM_KEYS);
        customKeys.addAll(CUSTOM_BLOCK_KEYS);
        return customKeys.toArray(new Key[0]);
    }

    private static class ListenerImpl implements Listener {

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onIslandGenerateBlock(IslandGenerateBlockEvent event) {
            if (!event.getBlock().getGlobalKey().equals(CRAFTENGINE_PREFIX))
                return;

            CustomBlock customBlock = CUSTOM_ITEM_TO_BLOCK_CACHE.get(event.getBlock());
            if (customBlock == null) {
                event.setCancelled(true);
                return;
            }

            event.setPlaceBlock(false);
            CraftEngineBlocks.place(event.getLocation(), customBlock.defaultState(), false);
        }

    }

    private static class CraftEngineKeyParser implements CustomKeyParser {

        @Override
        public Key getCustomKey(Location location) {
            Block block = location.getBlock();
            ImmutableBlockState customBlockState = CraftEngineBlocks.getCustomBlockState(block);
            if (customBlockState == null || customBlockState.isEmpty())
                return null;

            net.momirealms.craftengine.core.util.Key blockKey = customBlockState.owner().value().id();

            return Keys.of(CRAFTENGINE_PREFIX, blockKey.value().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public Key getCustomKey(ItemStack itemStack, Key def) {
            CustomItem<ItemStack> customItem = CraftEngineItems.byItemStack(itemStack);
            if (customItem == null)
                return def;

            net.momirealms.craftengine.core.util.Key itemKey = customItem.id();

            return Keys.of(CRAFTENGINE_PREFIX, itemKey.value().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public boolean isCustomKey(Key key) {
            return key.getGlobalKey().equals(CRAFTENGINE_PREFIX);
        }

        @Override
        @Nullable
        public ItemStack getCustomKeyItem(Key key) {
            return key.getGlobalKey().equals(CRAFTENGINE_PREFIX) ? CUSTOM_ITEM_CACHE.get(key) : null;
        }

    }

}
