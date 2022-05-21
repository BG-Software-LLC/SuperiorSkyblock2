package com.bgsoftware.superiorskyblock.hooks.support;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandGenerateBlockEvent;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.google.common.collect.ImmutableList;
import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.block.BlockMechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.stringblock.StringBlockMechanicFactory;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Locale;

public final class OraxenHook {

    private static final List<Pair<MechanicFactory, SetBlockModelFunction>> AVAILABLE_MECHANICS = ImmutableList.of(
            new Pair<>(MechanicsManager.getMechanicFactory("block"), BlockMechanicFactory::setBlockModel),
            new Pair<>(MechanicsManager.getMechanicFactory("noteblock"), NoteBlockMechanicFactory::setBlockModel),
            new Pair<>(MechanicsManager.getMechanicFactory("stringblock"), StringBlockMechanicFactory::setBlockModel)
    );

    public static void register(SuperiorSkyblockPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new GeneratorListener(), plugin);
    }

    private static final class GeneratorListener implements Listener {

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onIslandGenerateBlock(IslandGenerateBlockEvent event) {
            if (!event.getBlock().getGlobalKey().equals("ORAXEN"))
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

    private interface SetBlockModelFunction {

        void setBlockModel(Block block, String itemId);

    }

}
