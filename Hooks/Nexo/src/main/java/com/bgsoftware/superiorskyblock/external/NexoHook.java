package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandGenerateBlockEvent;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordFlags;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.external.blocks.ICustomBlocksProvider;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.mechanics.Mechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import com.nexomc.nexo.mechanics.MechanicsManager;
import com.nexomc.nexo.mechanics.custom_block.noteblock.NoteBlockMechanicFactory;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanicFactory;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class NexoHook {

    private static final String NEXO_PREFIX = "NEXO";
    private static final Key BLOCK_ITEM_KEY = Keys.of(Material.PAPER);
    private static final Key BLOCK_KEY = Keys.of(Material.NOTE_BLOCK);

    private static final List<MechanicData> AVAILABLE_MECHANICS = new LinkedList<>();

    private static final LazyReference<WorldRecordService> worldRecordService = new LazyReference<>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };

    private static SuperiorSkyblockPlugin plugin;

    public static void register(SuperiorSkyblockPlugin plugin) {
        NexoHook.plugin = plugin;
        plugin.getBlockValues().registerKeyParser(new NexoKeyParser(), BLOCK_ITEM_KEY, BLOCK_KEY);
        plugin.getProviders().registerCustomBlocksProvider(new NexuCustomBlocksProvider());
        plugin.getServer().getPluginManager().registerEvents(new NexoListener(), plugin);
        BukkitExecutor.sync(NexoHook::initializeMechanics, 1L);
    }

    private static void initializeMechanics() {
        MechanicsManager mechanicsManager = MechanicsManager.INSTANCE;
        AVAILABLE_MECHANICS.add(new MechanicData(mechanicsManager.getMechanicFactory("noteblock"), NoteBlockMechanicFactory.Companion::setBlockModel));
        AVAILABLE_MECHANICS.add(new MechanicData(mechanicsManager.getMechanicFactory("stringblock"), StringBlockMechanicFactory.Companion::setBlockModel));
    }

    private static class NexuCustomBlocksProvider implements ICustomBlocksProvider {

        @Nullable
        @Override
        public KeyMap<Integer> getBlockCountsForChunk(ChunkPosition chunkPosition) {
            if (!Bukkit.isPrimaryThread())
                return null;

            World world = chunkPosition.getWorld();
            if (!world.isChunkLoaded(chunkPosition.getX(), chunkPosition.getZ()))
                return KeyMaps.createEmptyMap();

            KeyMap<Integer> blockCounts = KeyMaps.createHashMap(KeyIndicator.CUSTOM);

            Chunk chunk = world.getChunkAt(chunkPosition.getX(), chunkPosition.getZ());

            for (Entity entity : chunk.getEntities()) {
                FurnitureMechanic mechanic = NexoFurniture.furnitureMechanic(entity);
                if (mechanic != null) {
                    Key blockKey = Keys.of(NEXO_PREFIX, mechanic.getItemID().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
                    blockCounts.put(blockKey, blockCounts.getRaw(blockKey, 0) + 1);
                }
            }

            return blockCounts;
        }
    }

    private static class NexoListener implements Listener {

        @WorldRecordFlags
        private static final int REGULAR_RECORD_FLAGS = WorldRecordFlags.SAVE_BLOCK_COUNT | WorldRecordFlags.DIRTY_CHUNKS;
        @WorldRecordFlags
        private static final int ALL_RECORD_FLAGS = REGULAR_RECORD_FLAGS | WorldRecordFlags.HANDLE_NEARBY_BLOCKS;

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onNexoBlockBreak(BlockBreakEvent e) {
            Key blockKey = Keys.of(e.getBlock());
            if (blockKey.getGlobalKey().equals(NEXO_PREFIX)) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    worldRecordService.get().recordBlockBreak(blockKey,
                            e.getBlock().getLocation(wrapper.getHandle()), 1, ALL_RECORD_FLAGS);
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onFurniturePlace(NexoFurniturePlaceEvent e) {
            Key blockKey = Keys.of(NEXO_PREFIX, e.getMechanic().getItemID().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                worldRecordService.get().recordBlockPlace(blockKey, e.getBlock().getLocation(wrapper.getHandle()),
                        1, null, REGULAR_RECORD_FLAGS);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onFurnitureBreak(NexoFurnitureBreakEvent e) {
            Key blockKey = Keys.of(NEXO_PREFIX, e.getMechanic().getItemID().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                worldRecordService.get().recordBlockBreak(blockKey,
                        e.getBaseEntity().getLocation(wrapper.getHandle()), 1, ALL_RECORD_FLAGS);
            }
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onIslandGenerateBlock(IslandGenerateBlockEvent event) {
            if (!event.getBlock().getGlobalKey().equals(NEXO_PREFIX))
                return;

            String itemId = event.getBlock().getSubKey().toLowerCase(Locale.ENGLISH);

            if (!NexoItems.exists(itemId)) {
                event.setCancelled(true);
                return;
            }

            for (MechanicData mechanic : AVAILABLE_MECHANICS) {
                if (mechanic.mechanicFactory != null && !mechanic.mechanicFactory.isNotImplementedIn(itemId)) {
                    event.setPlaceBlock(false);
                    mechanic.setBlockModelFunction.setBlockModel(event.getLocation().getBlock(), itemId);
                    return;
                }
            }

            // No mechanic was found
            event.setCancelled(true);
        }

    }

    private static class NexoKeyParser implements CustomKeyParser {

        @Override
        public Key getCustomKey(Location location) {
            Mechanic mechanic = NexoBlocks.customBlockMechanic(location);
            if (mechanic == null)
                return BLOCK_KEY;
            return Keys.of(NEXO_PREFIX, mechanic.getItemID().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public Key getCustomKey(ItemStack itemStack, Key def) {
            String itemId = NexoItems.idFromItem(itemStack);
            if (itemId == null)
                return def;
            return Keys.of(NEXO_PREFIX, itemId.toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public boolean isCustomKey(Key key) {
            return key.getGlobalKey().equals(NEXO_PREFIX);
        }

        @Override
        @Nullable
        public ItemStack getCustomKeyItem(Key key) {
            ItemBuilder itemBuilder = NexoItems.itemFromId(key.getSubKey().toLowerCase(Locale.ENGLISH));
            if (itemBuilder == null)
                return null;
            return itemBuilder.build();
        }

    }

    private record MechanicData(MechanicFactory mechanicFactory, SetBlockModelFunction setBlockModelFunction) {

    }

    private interface SetBlockModelFunction {

        void setBlockModel(Block block, String itemId);

    }

}
