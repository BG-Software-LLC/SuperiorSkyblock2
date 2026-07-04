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
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.item.ItemDefinition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class CraftEngineHook26 {
    private static final String CRAFTENGINE_PREFIX = "CRAFTENGINE";
    private static final KeySet ITEM_DEFINITION_KEYS = collectItemDefinitionKeys();
    private static final KeySet BLOCK_DEFINITION_KEYS = collectBlockDefinitionKeys();
    private static final KeyMap<ItemStack> ITEM_DEFINITIONS_CACHE = collectItemDefinitionsCache();
    private static final KeyMap<BlockDefinition> BLOCK_DEFINITION_TO_BLOCKS_CACHE = collectBlockDefinitionToBlocksCache();
    private static boolean registered = false;

    public static void register(SuperiorSkyblockPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new ListenerImpl(), plugin);
        if (!registered) {
            registered = true;
            plugin.getBlockValues().registerKeyParser(new CraftEngineKeyParser(), collectCustomKeys());
        }
    }

    private static KeySet collectItemDefinitionKeys() {
        KeySet itemDefinitionKeys = KeySets.createHashSet(KeyIndicator.MATERIAL);
        for (ItemDefinition definition : CraftEngineItems.loadedItems().values()) {
            ItemStack itemStack = ((BukkitItemDefinition) definition).buildBukkitItem();
            itemDefinitionKeys.add(Keys.of(itemStack.getType()));
        }
        return itemDefinitionKeys;
    }

    private static KeySet collectBlockDefinitionKeys() {
        KeySet blockDefinitionKeys = KeySets.createHashSet(KeyIndicator.MATERIAL);
        for (BlockDefinition definition : CraftEngineBlocks.loadedBlocks().values()) {
            BlockData blockData = CraftEngineBlocks.getBukkitBlockData(definition.defaultState());
            blockDefinitionKeys.add(Keys.of(blockData.getMaterial()));
        }
        return blockDefinitionKeys;
    }

    private static KeyMap<ItemStack> collectItemDefinitionsCache() {
        KeyMap<ItemStack> itemDefinitionsCache = KeyMaps.createHashMap(KeyIndicator.CUSTOM);
        CraftEngineItems.loadedItems().forEach((id, definition) -> {
            ItemStack itemStack = ((BukkitItemDefinition) definition).buildBukkitItem();
            Key ssbItemKey = Keys.of(CRAFTENGINE_PREFIX, toSubKey(id), KeyIndicator.CUSTOM);
            itemDefinitionsCache.put(ssbItemKey, itemStack);
        });
        return itemDefinitionsCache;
    }

    private static KeyMap<BlockDefinition> collectBlockDefinitionToBlocksCache() {
        KeyMap<BlockDefinition> blockDefinitionToBlocksCache = KeyMaps.createHashMap(KeyIndicator.CUSTOM);
        CraftEngineBlocks.loadedBlocks().forEach((id, definition) -> {
            Key ssbBlockKey = Keys.of(CRAFTENGINE_PREFIX, toSubKey(id), KeyIndicator.CUSTOM);
            blockDefinitionToBlocksCache.put(ssbBlockKey, definition);
        });
        return blockDefinitionToBlocksCache;
    }

    private static Key[] collectCustomKeys() {
        KeySet customKeys = KeySets.createHashSet(KeyIndicator.MATERIAL);
        customKeys.addAll(ITEM_DEFINITION_KEYS);
        customKeys.addAll(BLOCK_DEFINITION_KEYS);
        return customKeys.toArray(new Key[0]);
    }

    private static class ListenerImpl implements Listener {

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onIslandGenerateBlock(IslandGenerateBlockEvent event) {
            if (!event.getBlock().getGlobalKey().equals(CRAFTENGINE_PREFIX)) return;
            BlockDefinition definition = BLOCK_DEFINITION_TO_BLOCKS_CACHE.get(event.getBlock());
            if (definition == null) {
                event.setCancelled(true);
                return;
            }
            event.setPlaceBlock(false);
            CraftEngineBlocks.place(event.getLocation(), definition.defaultState(), false);
        }

    }

    private static class CraftEngineKeyParser implements CustomKeyParser {

        @Override
        public Key getCustomKey(Location location) {
            Block block = location.getBlock();
            ImmutableBlockState blockState = CraftEngineBlocks.getCustomBlockState(block);
            if (blockState == null || blockState.isEmpty()) return null;
            net.momirealms.craftengine.core.util.Key id = blockState.owner().value().id();
            return Keys.of(CRAFTENGINE_PREFIX, toSubKey(id), KeyIndicator.CUSTOM);
        }

        @Override
        public Key getCustomKey(ItemStack itemStack, Key def) {
            BukkitItemDefinition definition = CraftEngineItems.byItemStack(itemStack);
            if (definition == null) return def;
            return Keys.of(CRAFTENGINE_PREFIX, toSubKey(definition.id()), KeyIndicator.CUSTOM);
        }

        @Override
        public boolean isCustomKey(Key key) {
            return key.getGlobalKey().equals(CRAFTENGINE_PREFIX);
        }

        @Override
        @Nullable
        public ItemStack getCustomKeyItem(Key key) {
            return key.getGlobalKey().equals(CRAFTENGINE_PREFIX) ? ITEM_DEFINITIONS_CACHE.get(key) : null;
        }

    }

    private static String toSubKey(net.momirealms.craftengine.core.util.Key key) {
        // The namespace and value are user-defined and cannot be omitted.
        return (key.namespace + "_" + key.value).toUpperCase(Locale.ROOT);
    }
}
