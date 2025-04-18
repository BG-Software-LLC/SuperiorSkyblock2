package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandGenerateBlockEvent;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordFlags;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class ItemsAdderHook {

    private static final String ITEMS_ADDER_PREFIX = "ITEMS_ADDER";
    private static final Key BLOCK_ITEM_KEY = Keys.of(Material.PAPER);
    private static final Key BLOCK_KEY = Keys.of(Material.NOTE_BLOCK);

    private static final LazyReference<WorldRecordService> worldRecordService = new LazyReference<WorldRecordService>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };

    private static SuperiorSkyblockPlugin plugin;

    public static void register(SuperiorSkyblockPlugin plugin) {
        ItemsAdderHook.plugin = plugin;
        plugin.getBlockValues().registerKeyParser(new ItemsAdderKeyParser(), BLOCK_ITEM_KEY, BLOCK_KEY);
        plugin.getServer().getPluginManager().registerEvents(new ListenerImpl(), plugin);
    }

    private static class ListenerImpl implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onItemAdderBlockInteract(PlayerInteractEvent e) {
            switch (e.getAction()) {
                case RIGHT_CLICK_BLOCK:
                    onBlockPlace(e);
                    break;
                case LEFT_CLICK_BLOCK:
                    onBlockBreak(e);
                    break;
            }
        }

        private void onBlockPlace(PlayerInteractEvent e) {
            if (e.getItem() == null)
                return;

            CustomBlock customBlock = CustomBlock.byItemStack(e.getItem());
            if (customBlock == null)
                return;

            CustomBlock clickedBlock = CustomBlock.byAlreadyPlaced(e.getClickedBlock());
            if (clickedBlock != null)
                return;

            Block placeBlock = e.getClickedBlock().getRelative(e.getBlockFace());

            BukkitExecutor.sync(() -> {
                if (placeBlock.getType() == Material.NOTE_BLOCK) {
                    try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                        Key itemKey = Keys.of(ITEMS_ADDER_PREFIX, customBlock.getId().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
                        worldRecordService.get().recordBlockPlace(itemKey,
                                placeBlock.getLocation(wrapper.getHandle()), 1,
                                null, WorldRecordFlags.SAVE_BLOCK_COUNT | WorldRecordFlags.DIRTY_CHUNKS);
                    }
                }
            }, 1L);
        }

        private void onBlockBreak(PlayerInteractEvent e) {
            CustomBlock clickedBlock = CustomBlock.byAlreadyPlaced(e.getClickedBlock());
            if (clickedBlock != null) {
                Key blockKey = Key.of(e.getClickedBlock());
                BukkitExecutor.sync(() -> {
                    if (e.getClickedBlock().getType() == Material.AIR) {
                        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                            worldRecordService.get().recordBlockBreak(blockKey,
                                    e.getClickedBlock().getLocation(wrapper.getHandle()),
                                    1,
                                    WorldRecordFlags.SAVE_BLOCK_COUNT | WorldRecordFlags.DIRTY_CHUNKS | WorldRecordFlags.HANDLE_NEARBY_BLOCKS);
                        }
                    }
                }, 1L);

            }
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onIslandGenerateBlock(IslandGenerateBlockEvent event) {
            if (!event.getBlock().getGlobalKey().equals(ITEMS_ADDER_PREFIX))
                return;

            String itemId = event.getBlock().getSubKey().toLowerCase(Locale.ENGLISH);

            CustomBlock customBlock = CustomBlock.getInstance(itemId);

            if (customBlock == null) {
                event.setCancelled(true);
                return;
            }

            event.setPlaceBlock(false);
            customBlock.place(event.getLocation());
        }

    }

    private static class ItemsAdderKeyParser implements CustomKeyParser {

        @Override
        public Key getCustomKey(Location location) {
            Block block = location.getBlock();
            CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
            if (customBlock == null)
                return BLOCK_KEY;
            return Keys.of(ITEMS_ADDER_PREFIX, customBlock.getId().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public Key getCustomKey(ItemStack itemStack, Key def) {
            CustomStack customStack = CustomStack.byItemStack(itemStack);
            if (customStack == null)
                return def;
            return Keys.of(ITEMS_ADDER_PREFIX, customStack.getId().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public boolean isCustomKey(Key key) {
            return key.getGlobalKey().equals(ITEMS_ADDER_PREFIX);
        }

        @Override
        @Nullable
        public ItemStack getCustomKeyItem(Key key) {
            CustomStack customStack = CustomStack.getInstance(key.getSubKey().toLowerCase(Locale.ENGLISH));
            if (customStack == null)
                return null;
            return customStack.getItemStack();
        }

    }

}
