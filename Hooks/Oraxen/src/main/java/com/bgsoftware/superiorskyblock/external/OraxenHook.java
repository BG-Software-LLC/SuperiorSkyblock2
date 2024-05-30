package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandGenerateBlockEvent;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordFlags;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.block.BlockMechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.stringblock.StringBlockMechanicFactory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

    private static final List<Pair<MechanicFactory, SetBlockModelFunction>> AVAILABLE_MECHANICS;

    static {
        List<Pair<MechanicFactory, SetBlockModelFunction>> availableMechanics = new LinkedList<>();

        try {
            Class.forName("io.th0rgal.oraxen.mechanics.provided.gameplay.block.BlockMechanicFactory");
            availableMechanics.add(new Pair<>(MechanicsManager.getMechanicFactory("block"), BlockMechanicFactory::setBlockModel));
            availableMechanics.add(new Pair<>(MechanicsManager.getMechanicFactory("noteblock"), NoteBlockMechanicFactory::setBlockModel));
            availableMechanics.add(new Pair<>(MechanicsManager.getMechanicFactory("stringblock"), StringBlockMechanicFactory::setBlockModel));
        } catch (Throwable error) {
            availableMechanics.add(new Pair<>(MechanicsManager.getMechanicFactory("block"), (block, itemId) -> {
                ReflectMethod<Void> setBlockModel = new ReflectMethod<>(
                        new ClassInfo("io.th0rgal.oraxen.mechanics.provided.block.BlockMechanicFactory", ClassInfo.PackageType.UNKNOWN),
                        "setBlockModel", Block.class, String.class);
                if (setBlockModel.isValid())
                    setBlockModel.invoke(null, block, itemId);
            }));
            availableMechanics.add(new Pair<>(MechanicsManager.getMechanicFactory("noteblock"), (block, itemId) -> {
                ReflectMethod<Void> setBlockModel = new ReflectMethod<>(
                        new ClassInfo("io.th0rgal.oraxen.mechanics.provided.noteblock.NoteBlockMechanicFactory", ClassInfo.PackageType.UNKNOWN),
                        "setBlockModel", Block.class, String.class);
                if (setBlockModel.isValid())
                    setBlockModel.invoke(null, block, itemId);
            }));
        }

        AVAILABLE_MECHANICS = Collections.unmodifiableList(availableMechanics);
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
        plugin.getServer().getPluginManager().registerEvents(new GeneratorListener(), plugin);
    }

    private static class GeneratorListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onOraxenBlockBreak(BlockBreakEvent e) {
            Key blockKey = Keys.of(e.getBlock());
            if (blockKey.getGlobalKey().equals(ORAXEN_PREFIX)) {
                worldRecordService.get().recordBlockBreak(blockKey, e.getBlock().getLocation(), 1,
                        WorldRecordFlags.SAVE_BLOCK_COUNT | WorldRecordFlags.DIRTY_CHUNKS | WorldRecordFlags.HANDLE_NEARBY_BLOCKS);
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

            Pair<MechanicFactory, SetBlockModelFunction> mechanic = AVAILABLE_MECHANICS.stream()
                    .filter(pair -> pair.getKey() != null && !pair.getKey().isNotImplementedIn(itemId))
                    .findFirst().orElse(null);

            if (mechanic == null) {
                event.setCancelled(true);
                return;
            }

            event.setPlaceBlock(false);
            mechanic.getValue().setBlockModel(event.getLocation().getBlock(), itemId);
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

    private interface SetBlockModelFunction {

        void setBlockModel(Block block, String itemId);

    }

}
