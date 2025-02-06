package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectMethod;
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
import com.bgsoftware.superiorskyblock.external.blocks.ICustomBlocksProvider;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenFurniture;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.furniture.OraxenFurnitureBreakEvent;
import io.th0rgal.oraxen.api.events.furniture.OraxenFurniturePlaceEvent;
import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.block.BlockMechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.stringblock.StringBlockMechanicFactory;
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class OraxenHook {

    private static final String ORAXEN_PREFIX = "ORAXEN";
    private static final Key BLOCK_ITEM_KEY = Keys.of(Material.PAPER);
    private static final Key BLOCK_KEY = Keys.of(Material.NOTE_BLOCK);

    private static final List<MechanicData> AVAILABLE_MECHANICS = initializeMechanics();

    private static List<MechanicData> initializeMechanics() {
        List<MechanicData> mechanics = new LinkedList<>();

        try {
            Class.forName("io.th0rgal.oraxen.mechanics.provided.gameplay.block.BlockMechanicFactory");
            mechanics.add(new MechanicData(MechanicsManager.getMechanicFactory("block"), BlockMechanicFactory::setBlockModel));
            mechanics.add(new MechanicData(MechanicsManager.getMechanicFactory("noteblock"), NoteBlockMechanicFactory::setBlockModel));
            mechanics.add(new MechanicData(MechanicsManager.getMechanicFactory("stringblock"), StringBlockMechanicFactory::setBlockModel));
        } catch (Throwable error) {
            {
                ReflectMethod<Void> setBlockModel = new ReflectMethod<>(
                        new ClassInfo("io.th0rgal.oraxen.mechanics.provided.block.BlockMechanicFactory", ClassInfo.PackageType.UNKNOWN),
                        "setBlockModel", Block.class, String.class);

                mechanics.add(new MechanicData(MechanicsManager.getMechanicFactory("block"), (block, itemId) -> {
                    if (setBlockModel.isValid())
                        setBlockModel.invoke(null, block, itemId);
                }));
            }

            {
                ReflectMethod<Void> setBlockModel = new ReflectMethod<>(
                        new ClassInfo("io.th0rgal.oraxen.mechanics.provided.noteblock.NoteBlockMechanicFactory", ClassInfo.PackageType.UNKNOWN),
                        "setBlockModel", Block.class, String.class);

                mechanics.add(new MechanicData(MechanicsManager.getMechanicFactory("noteblock"), (block, itemId) -> {
                    if (setBlockModel.isValid())
                        setBlockModel.invoke(null, block, itemId);
                }));
            }
        }

        return Collections.unmodifiableList(mechanics);
    }

    private static final LazyReference<WorldRecordService> worldRecordService = new LazyReference<WorldRecordService>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };

    private static SuperiorSkyblockPlugin plugin;

    public static void register(SuperiorSkyblockPlugin plugin) {
        OraxenHook.plugin = plugin;
        plugin.getBlockValues().registerKeyParser(new OraxenKeyParser(), BLOCK_ITEM_KEY, BLOCK_KEY);
        plugin.getProviders().registerCustomBlocksProvider(new OraxenCustomBlocksProvider());
        plugin.getServer().getPluginManager().registerEvents(new OraxenListener(), plugin);
    }

    private static class OraxenCustomBlocksProvider implements ICustomBlocksProvider {

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
                FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(entity);
                if (mechanic != null) {
                    Key blockKey = Keys.of(ORAXEN_PREFIX, mechanic.getItemID().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
                    blockCounts.put(blockKey, blockCounts.getRaw(blockKey, 0) + 1);
                }
            }

            return blockCounts;
        }
    }

    private static class OraxenListener implements Listener {

        @WorldRecordFlags
        private static final int REGULAR_RECORD_FLAGS = WorldRecordFlags.SAVE_BLOCK_COUNT | WorldRecordFlags.DIRTY_CHUNKS;
        @WorldRecordFlags
        private static final int ALL_RECORD_FLAGS = REGULAR_RECORD_FLAGS | WorldRecordFlags.HANDLE_NEARBY_BLOCKS;

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onOraxenBlockBreak(BlockBreakEvent e) {
            Key blockKey = Keys.of(e.getBlock());
            if (blockKey.getGlobalKey().equals(ORAXEN_PREFIX)) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    worldRecordService.get().recordBlockBreak(blockKey,
                            e.getBlock().getLocation(wrapper.getHandle()), 1, ALL_RECORD_FLAGS);
                }


            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onFurniturePlace(OraxenFurniturePlaceEvent e) {
            Key blockKey = Keys.of(ORAXEN_PREFIX, e.getMechanic().getItemID().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                worldRecordService.get().recordBlockPlace(blockKey, e.getBlock().getLocation(wrapper.getHandle()),
                        1, null, REGULAR_RECORD_FLAGS);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onFurnitureBreak(OraxenFurnitureBreakEvent e) {
            Key blockKey = Keys.of(ORAXEN_PREFIX, e.getMechanic().getItemID().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                worldRecordService.get().recordBlockBreak(blockKey,
                        e.getBaseEntity().getLocation(wrapper.getHandle()), 1, ALL_RECORD_FLAGS);
            }
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onIslandGenerateBlock(IslandGenerateBlockEvent event) {
            if (!event.getBlock().getGlobalKey().equals(ORAXEN_PREFIX))
                return;

            String itemId = event.getBlock().getSubKey().toLowerCase(Locale.ENGLISH);

            if (!OraxenItems.exists(itemId)) {
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

    private static class OraxenKeyParser implements CustomKeyParser {

        @Override
        public Key getCustomKey(Location location) {
            Mechanic mechanic = OraxenBlocks.getOraxenBlock(location);
            if (mechanic == null)
                return BLOCK_KEY;
            return Keys.of(ORAXEN_PREFIX, mechanic.getItemID().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public Key getCustomKey(ItemStack itemStack, Key def) {
            String itemId = OraxenItems.getIdByItem(itemStack);
            if (itemId == null)
                return def;
            return Keys.of(ORAXEN_PREFIX, itemId.toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public boolean isCustomKey(Key key) {
            return key.getGlobalKey().equals(ORAXEN_PREFIX);
        }

        @Override
        @Nullable
        public ItemStack getCustomKeyItem(Key key) {
            ItemBuilder itemBuilder = OraxenItems.getItemById(key.getSubKey().toLowerCase(Locale.ENGLISH));
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
